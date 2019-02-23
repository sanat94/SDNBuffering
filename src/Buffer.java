import java.awt.List;
import java.util.ArrayList;
import java.util.Queue;

import com.google.common.collect.EvictingQueue;

class Buffer{
	Queue<Chunk> queue;
	Buffer(int i){
		this.queue = EvictingQueue.create(i);
	}
	Chunk getChunk() {
		if(!queue.isEmpty())
			return queue.remove();
		else
			return null;
	}
	void addChunk(Chunk chk){
		queue.add(chk);
	}
	Chunk getChunk(int id) {
		for(Chunk chunk: queue) {
			if(chunk.getId()==id)
				return chunk;
		}
		return null;	
	}
	Boolean containsId(int id) {
		ArrayList<Integer> idList = new ArrayList<Integer>();
		for(Chunk chunk: queue) {
			idList.add(chunk.getId());
		}
		if(idList.contains(id))
			return true;
		else
			return false;
	}
	int getFirstId() {
		if(!queue.isEmpty())
			return queue.peek().getId();
		else
			return -1;
	}
}