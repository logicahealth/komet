package org.ihtsdo.otf.tcc.api.coordinate;

import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;


public class EditCoordinate {
	private int authorNid;
        private int moduleNid;
	private NidSetBI editPaths;
	
	public EditCoordinate(int authorNid, int moduleNid, NidSetBI editPaths) {
		super();
		assert editPaths != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
                this.moduleNid = moduleNid;
		this.editPaths = editPaths;
	}
	
	public EditCoordinate(int authorNid, int moduleNid, int... editPathNids) {
		super();
		assert editPathNids != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
                this.moduleNid = moduleNid;
		this.editPaths = new NidSet(editPathNids);
	}

	public int getAuthorNid() {
		return authorNid;
	}
        
        public int getModuleNid() {
		return moduleNid;
	}

	public NidSetBI getEditPaths() {
		return editPaths;
	}
   
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("authorNid: ").append(authorNid);
      sb.append("moduleNid: ").append(moduleNid);
      sb.append("editPaths: ").append(editPaths);           
      return sb.toString();
   }

}
