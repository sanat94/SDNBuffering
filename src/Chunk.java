import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

class Chunk{
	int id;
	int videoId;
	List<Frame> frames;
	protected Chunk(int id, int videoId) {
		super();
		this.id = id;
		this.videoId = videoId;
		this.frames = new ArrayList<Frame>();
	}
	protected int getVideoId() {
		return videoId;
	}
	protected void setVideoId(int videoId) {
		this.videoId = videoId;
	}
	protected int getId() {
		return id;
	}
	protected void setId(int id) {
		this.id = id;
	}
	protected List<Frame> getFrames() {
		return frames;
	}
	protected void setFrames(List<Frame> frames) {
		this.frames = frames;
	}
	protected void addFrame(Frame frame) {
		this.frames.add(frame);
	}
	
}