import java.awt.Image;
import java.io.File;
import java.io.Serializable;

class Frame implements Serializable{
	int id;
	String frameType;
	int videoId;
	int chunkId;
	//byte[] imgFile;
	private static final long serialVersionUID = 1113799434508676095L;

	protected Frame(int id, String frameType, int videoId, int chunkId) {
		super();
		this.id = id;
		this.frameType = frameType;
		this.videoId = videoId;
		this.chunkId = chunkId;
		//this.imgFile = img;
	}
	protected int getId() {
		return id;
	}
	protected void setId(int id) {
		this.id = id;
	}
	protected String getFrameType() {
		return frameType;
	}
	protected void setFrameType(String frameType) {
		this.frameType = frameType;
	}
	protected int getVideoId() {
		return videoId;
	}
	protected void setVideoId(int videoId) {
		this.videoId = videoId;
	}
	protected int getChunkId() {
		return chunkId;
	}
	protected void setChunkId(int chunkId) {
		this.chunkId = chunkId;
	}
	/*protected byte[] getImg() {
		return imgFile;
	}
	protected void setImg(byte[] img) {
		this.imgFile = img;
	}*/
	
}
