package de.swa.ui;

import de.swa.gc.GraphCode;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.builder.FeatureVectorBuilder;
import de.swa.mmfg.builder.XMLEncodeDecode;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by Patrick Steinert on 27.12.23.
 */
public abstract class MMFGCollection {
	/**
	 * singleton pattern access
	 **/
	private static MMFGCollection instance;

	/** singleton pattern access **/
/*	protected static synchronized MMFGCollection getInstance() {
		if (instance == null) {
			instance = new MMFGCollection();
		}
		//instance.init();
		return instance;
	}
  */
	/** session facade pattern access **/
//	private static Hashtable<String, MvMMFGCollection> sessions = new Hashtable<String, MvMMFGCollection>();

//	protected static synchronized MvMMFGCollection getInstance(String session_id) {
//		if (sessions.get(session_id) != null)
//			return sessions.get(session_id);
//		else {
////			MMFGCollection coll = new MMFGCollection();
////			coll.init();
////			sessions.put(session_id, coll);
////			return coll;
//
//			sessions.put(session_id, getInstance());
//			return instance;
//		}
//	}

	public abstract void addProgressListener(ProgressListener pl);

	public abstract void addRefreshListener(RefreshListener re);

	public abstract void refresh();

	public abstract void init();

	public MMFG loadFromMMFGFile(File existingMMFG) {
		try {
			String content = "";
			List<String> lines = Files.readAllLines(existingMMFG.toPath());
			content = String.join("\n", lines);
			MMFG mmfg = FeatureVectorBuilder.unflatten(content, new XMLEncodeDecode());
			return mmfg;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new MMFG();
	}

	public abstract void addToCollection(MMFG m);

	public abstract Vector<MMFG> getCollection();

	public abstract void replaceMMFGInCollection(MMFG m, File f);

	public abstract String getName();

	public abstract MMFG getMMFGForFile(File f);

	public abstract MMFG getMMFGForId(UUID id);

	public abstract GraphCode getCurrentQuery();

	public abstract Vector<GraphCode> getCollectionGraphCodes();

	public abstract GraphCode getOrGenerateGraphCode(MMFG mmfg);

	public abstract Vector<MMFG> getSimilarAssets(GraphCode gcQuery);

	public abstract int getIndexForAsset(MMFG m);

	public abstract Vector<MMFG> processQuery(GraphCode gcQuery, int type);

	public abstract Vector<MMFG> getRecommendedAssets(GraphCode gcQuery);

	public abstract void query(GraphCode gcQuery);

	public abstract Model getRDFModel();
}
