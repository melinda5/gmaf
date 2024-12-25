package de.swa.ui;

import de.swa.gc.GraphCode;
import de.swa.gc.GraphCodeGenerator;
import de.swa.gc.GraphCodeIO;
import de.swa.gc.processing.CollectionProcessor;
import de.swa.gc.processing.DefaultCollectionProcessor;
import de.swa.gc.processing.GraphCodeMeta;
import de.swa.gmaf.GMAF;
import de.swa.mmfg.CompositionRelationship;
import de.swa.mmfg.GeneralMetadata;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.mmfg.builder.FeatureVectorBuilder;
import de.swa.mmfg.builder.XMLEncodeDecode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by Patrick Steinert on 27.12.23.
 */
public class MvMMFGCollection extends MMFGCollection {
	private Vector<MMFG> collection = new Vector<MMFG>();
	private Hashtable<File, MMFG> fileMap = new Hashtable<File, MMFG>();
	private String name = "";
	private Model model = ModelFactory.createDefaultModel();

	private static MvMMFGCollection instance;



	private boolean inited = false;

	protected MvMMFGCollection() {
	}

	private Hashtable<UUID, MMFG> idMap = new Hashtable<UUID, MMFG>();
	private Vector<ProgressListener> progressListeners = new Vector<ProgressListener>();
	public void addProgressListener(ProgressListener pl) { progressListeners.add(pl); }

	private Vector<RefreshListener> refreshListeners = new Vector<RefreshListener>();
	public void addRefreshListener(RefreshListener re) { refreshListeners.add(re); }

	public void refresh() {
		for (RefreshListener re : refreshListeners) re.refresh();
	}

	/** initializes the collection and the configuration **/
	public synchronized void init() {
		if (inited)
			return;
		synchronized (this) {
			inited = true;
		}
		GMAF gmaf = new GMAF();
		name = Configuration.getInstance().getCollectionName();

		try {
			File rdfFolder = new File(Configuration.getInstance().getRDFRepo() + File.separatorChar);
			File[] rdfFiles = rdfFolder.listFiles();
			for (File rdf : rdfFiles) {
				if (rdf.isFile()) {
					InputStream in = RDFDataMgr.open(rdf.getPath());
					model.read(in, null);
				}
			}

			Vector<String> paths = Configuration.getInstance().getCollectionPaths();
			if (paths == null)
				return;
			Vector<String> fileExtensions = Configuration.getInstance().getFileExtensions();
			for (String path : paths) {
				File f = new File(path);
				try {
					File[] fs = f.listFiles();
					int count = 0;
					for (File fi : fs) {
						count++;
						int progress = 100 * count / fs.length;
						String txt = "loading " + path + " > " + fi.getName() + " (" + count + " / " + fs.length + ")";
						System.out.println(progress + " -> " + txt);
						for (ProgressListener pl : progressListeners) {
							pl.log(progress, txt);
						}

						try {
							String fileName = fi.getName();
							String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
							if (fileExtensions.contains(ext)) {
								if (fi.getName().endsWith(".wapo")) {
									FileInputStream fsx = new FileInputStream(fi);
									byte[] bytes = fsx.readAllBytes();
									MMFG mmfg = gmaf.processAsset(bytes, fi.getName(), "system",
											Configuration.getInstance().getMaxRecursions(),
											Configuration.getInstance().getMaxNodes(), f.getName(), f);
									GeneralMetadata gm = new GeneralMetadata();
									gm.setFileName(fileName);
									gm.setFileReference(fi);
									mmfg.setGeneralMetadata(gm);
									addToCollection(mmfg);
								} else {
									MMFG mmfg = new MMFG();
									GeneralMetadata gm = new GeneralMetadata();
									gm.setFileName(fileName);
									gm.setFileReference(fi);
									mmfg.setGeneralMetadata(gm);

									File existingMMFG = new File(Configuration.getInstance().getMMFGRepo()
											+ File.separatorChar + fileName + ".mmfg");
									if (existingMMFG.exists()) {
										mmfg = loadFromMMFGFile(existingMMFG);
										if (mmfg.getGeneralMetadata() == null)
											mmfg.setGeneralMetadata(gm);
										if (mmfg.getGeneralMetadata().getFileReference() == null)
											mmfg.getGeneralMetadata().setFileReference(fi);
									}
									addToCollection(mmfg);
								}
								if (Configuration.getInstance().getSelectedAsset() == null)
									Configuration.getInstance().setSelectedAsset(fi);
							}
						} catch (Exception x) {
							x.printStackTrace();
						}
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
			Vector<MMFG> tmpSubgraphs = new Vector<MMFG>();

			Vector<MMFG> mmfgs = collection; //getCollection();
			for (MMFG mmfg : mmfgs) {
				Vector<Node> nodes = mmfg.getNodes();
				for (Node node : nodes) {
					System.out.println("Node: " + node.getName());
					if (node.getName().contains("shot")) {
						System.out.println("Subgraph found " + mmfg.getGeneralMetadata().getFileName() + " node name " + node.getName());
						MMFG subgraph = new MMFG();
						GeneralMetadata gm = subgraph.getGeneralMetadata();
						gm.setFileName(mmfg.getGeneralMetadata().getFileName());
						gm.setFileReference(mmfg.getGeneralMetadata().getFileReference());
						gm.setCameraModel("Subgraph-" + node.getName() + "-" + gm.getFileName());
						subgraph.setGeneralMetadata(gm);

						Node scene = new Node(node.getName(), subgraph);
						subgraph.addNode(scene);

						Vector<Node> children = node.getChildNodes();
						for (Node child : children) {
							Node node1 = new Node(child.getName(), subgraph);
							scene.addChildNode(node1);
							CompositionRelationship cr = new CompositionRelationship(CompositionRelationship.RELATION_PART_OF, scene);

							node1.addCompositionRelationship(cr);
							//scene.addCompositionRelationship(new CompositionRelationship(CompositionRelationship.RELATION_PART_OF, node1));
							//subgraph.addNode(child);
						}

						tmpSubgraphs.add(subgraph);

					}
				}
			}
			for (MMFG subgraph : tmpSubgraphs) {
				System.out.println("Adding Subgraph: " + subgraph.getGeneralMetadata().getCameraModel());
				addToCollection(subgraph);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		inited = true;
	}



	/** adds a MMFG to the collection **/
	public void addToCollection(MMFG m) {
		collection.add(m);
		fileMap.put(m.getGeneralMetadata().getFileReference(), m);
		idMap.put(m.getGeneralMetadata().getId(), m);
	}

	/** returns the collection of MMFGs **/
	public Vector<MMFG> getCollection() {
		return collection;
	}

	/** replaces an element of the collection **/
	public void replaceMMFGInCollection(MMFG m, File f) {
		try {
			MMFG old = getMMFGForFile(f);
			graphCodeCache.remove(old);
			collection.remove(old);
			fileMap.remove(f);
			addToCollection(m);
		} catch (Exception x) {
			x.printStackTrace();
		}
		System.out.println("MMFG replaced in collection for file " + f.getAbsolutePath());
	}

	/** returns the collection name **/
	public String getName() {
		return name;
	}

	/** returns the MMFG for a given file **/
	public MMFG getMMFGForFile(File f) {
		return fileMap.get(f);
	}

	public MMFG getMMFGForId(UUID id) {
		if (idMap.containsKey(id)) return idMap.get(id);
		for (MMFG m : collection) {
			if (m.getId().equals(id)) {
				idMap.put(id, m);
				return m;
			}
		}
		return null;
	}

	private GraphCode currentQuery;

	public GraphCode getCurrentQuery() {
		return currentQuery;
	}

	/** returns a vector of all collection's Graph Codes **/
	public Vector<GraphCode> getCollectionGraphCodes() {
		Vector<GraphCode> v = new Vector<GraphCode>();
		for (MMFG m : collection) {
			GraphCode gc = getOrGenerateGraphCode(m);
			v.add(gc);
		}
		return v;
	}

	/** returns or generates a Graph Code for a given MMFG **/
	public GraphCode getOrGenerateGraphCode(MMFG mmfg) {
		if (graphCodeCache.containsKey(mmfg))
			return graphCodeCache.get(mmfg);
		GraphCode gc = GraphCodeGenerator.generate(mmfg);
		graphCodeCache.put(mmfg, gc);
		return gc;
	}

	/** returns similar assets based on a Graph Code query **/
	public Vector<MMFG> getSimilarAssets(GraphCode gcQuery) {
		return processQuery(gcQuery, CollectionProcessor.SIMILARITY);
	}

	public int getIndexForAsset(MMFG m) {
		return collection.indexOf(m);
	}

	public Vector<MMFG> processQuery(GraphCode gcQuery, int type) {
		CollectionProcessor cp = new DefaultCollectionProcessor();
		try {
			String collectionProcessorClass = Configuration.getInstance().getCollectionProcessorClass();
			Class c = Class.forName(collectionProcessorClass);
			cp = (CollectionProcessor) c.newInstance();
		} catch (Exception ex) {
			System.out.println("no collection processor defined.");
			System.out.println("using " + cp.getClass() + " instead");
		}
		cp.setOperation(type);

		Vector<GraphCodeMeta> v = new Vector<GraphCodeMeta>();
		Hashtable<String, MMFG> queryCache = new Hashtable<String, MMFG>();

		for (MMFG m : collection) {
			GraphCode gc = null;
			File f = new File(Configuration.getInstance().getGraphCodeRepository() + File.separatorChar
					+ m.getGeneralMetadata().getFileName() + "_" + m.getGeneralMetadata().getCameraModel() + ".gc");
			if (graphCodeCache.containsKey(m))
				gc = graphCodeCache.get(m);
			else if (f.exists()) {
				gc = GraphCodeIO.read(f);
				graphCodeCache.put(m, gc);
			}
			if (gc == null) {
				gc = GraphCodeGenerator.generate(m);
				if (gc.getDictionary().size() > 1) {
					GraphCodeIO.write(gc, f);
					graphCodeCache.put(m, gc);
				}
			}

			String gcName = m.getGeneralMetadata().getFileName() + m.getGeneralMetadata().getCameraModel() + ".gc";
			GraphCodeMeta gcm = new GraphCodeMeta(gcName, gc);
			queryCache.put(gcName, m);
			v.add(gcm);
		}

		cp.setQueryObject(gcQuery);
		cp.preloadIndex(v);
		cp.execute();
		v = cp.getResultList();

		Vector<MMFG> tempCollection = new Vector<MMFG>();
		for (GraphCodeMeta gcm : v) {
			MMFG m = queryCache.get(gcm.getFileName());
			m.setTempSimilarity(gcm.getMetric());
			tempCollection.add(m);
		}
		collection = tempCollection;
		return tempCollection;
	}

	public static boolean isQuery = false;

	/** returns recommended assets based on a Graph Code query **/
	public Vector<MMFG> getRecommendedAssets(GraphCode gcQuery) {
		return processQuery(gcQuery, CollectionProcessor.RECOMMENDATION);
	}

	private Hashtable<MMFG, GraphCode> graphCodeCache = new Hashtable<MMFG, GraphCode>();

	/** executes a query **/
	public void query(GraphCode gcQuery) {
		currentQuery = gcQuery;

		processQuery(gcQuery, CollectionProcessor.SIMILARITY);
		for (RefreshListener re : refreshListeners) re.refresh();
	}

	public Model getRDFModel() {
		return model;
	}


}
