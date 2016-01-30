import java.io.*;

class Disk {

	public final static int BLOCK_SIZE = 512;
	public final static int NUM_BLOCKS = 1000;
	public final static int POINTERS_PER_BLOCK = (BLOCK_SIZE / 4);
	private int readCount = 0;
	private int writeCount = 0;
	private File fileName;
	private RandomAccessFile disk;

	public Disk() {
		try {
			fileName = new File("DISK");
			disk = new RandomAccessFile(fileName, "rw");
		} catch (IOException e) {
			System.err.println("Não foi possível iniciar o disco");
			System.exit(1);
		}
	}

	private void seek(int blocknum) throws IOException {
		if (blocknum < 0 || blocknum >= NUM_BLOCKS)
			throw new RuntimeException("Tentativa de Leitura " + blocknum
					+ " está fora da faixa");
		disk.seek((long) (blocknum * BLOCK_SIZE));
	}

	public void read(int blocknum, byte[] buffer) {
		if (buffer.length != BLOCK_SIZE)
			throw new RuntimeException("Tamanho do buffer:" + buffer.length);
		try {
			seek(blocknum);
			disk.read(buffer);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		readCount++;
	}

	public void read(int blocknum, SuperBlock block) {
		try {
			seek(blocknum);
			block.size = disk.readInt();
			block.iSize = disk.readInt();
			block.freeList = disk.readInt();
		} catch (EOFException e) {
			if (blocknum != 0) {
				System.err.println(e);
				System.exit(1);
			}
			block.size = block.iSize = block.freeList = 0;
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		readCount++;
	}

	public void read(int blocknum, InodeBlock block) {
		try {
			seek(blocknum);
			for (int i = 0; i < block.node.length; i++) {
				block.node[i].flags = disk.readInt();
				block.node[i].owner = disk.readInt();
				block.node[i].fileSize = disk.readInt();
				for (int j = 0; j < 13; j++)
					block.node[i].pointer[j] = disk.readInt();
			}
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		readCount++;
	}

	public void read(int blocknum, IndirectBlock block) {
		try {
			seek(blocknum);
			for (int i = 0; i < block.pointer.length; i++)
				block.pointer[i] = disk.readInt();
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		readCount++;
	}

	public void write(int blocknum, byte[] buffer) {
		if (buffer.length != BLOCK_SIZE)
			throw new RuntimeException("Write: bad buffer size "
					+ buffer.length);
		try {
			seek(blocknum);
			disk.write(buffer);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		writeCount++;
	}

	public void write(int blocknum, SuperBlock block) {
		try {
			seek(blocknum);
			disk.writeInt(block.size);
			disk.writeInt(block.iSize);
			disk.writeInt(block.freeList);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		writeCount++;
	}

	public void write(int blocknum, InodeBlock block) {
		try {
			seek(blocknum);
			for (int i = 0; i < block.node.length; i++) {
				disk.writeInt(block.node[i].flags);
				disk.writeInt(block.node[i].fileSize);
				for (int j = 0; j < 13; j++)
					disk.writeInt(block.node[i].pointer[j]);
			}
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		writeCount++;
	}

	public void write(int blocknum, IndirectBlock block) {
		try {
			seek(blocknum);
			for (int i = 0; i < block.pointer.length; i++)
				disk.writeInt(block.pointer[i]);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		writeCount++;
	}

	public void stop(boolean removeFile) {
		System.out.println(generateStats());
		if (removeFile)
			fileName.delete();
	}

	public void stop() {
		stop(true);
	}

	public String generateStats() {
		return ("DISK: Leitura: " + readCount + " Escrita: " + writeCount);
	}
}