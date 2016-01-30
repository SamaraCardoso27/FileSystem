import java.io.File;
import java.util.*;

class JavaFileSystem implements FileSystem {

	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;
	public int IBLOCK_SIZE = 8;
	private LinkedList<Integer> freeList;
	private SuperBlock SuperBlock;
	private FileTable FileTable;
	private Disk Disk;
	private int MAX_DIRECTBLOCK_SIZE = Disk.BLOCK_SIZE * 10;
	private int SINGLE_INDIRECTBLOCK_SIZE = Disk.BLOCK_SIZE * Disk.POINTERS_PER_BLOCK;
	private int DOUBLE_INDIRECTBLOCK_SIZE = SINGLE_INDIRECTBLOCK_SIZE * Disk.POINTERS_PER_BLOCK;
	private int TRIPLE_INDIRECTBLOCK_SIZE = DOUBLE_INDIRECTBLOCK_SIZE * Disk.POINTERS_PER_BLOCK;
	private static int iNumber_counter = 0;

	JavaFileSystem() {
		Disk = new Disk();
		FileTable = new FileTable();
	}

	public int formatDisk(int size, int iSize) {
		if (size < iSize || size == 0 || iSize == 0) {
			return -1;
		}
		SuperBlock = new SuperBlock();
		SuperBlock.size = size;
		SuperBlock.iSize = iSize;
		Disk.write(0, SuperBlock);
		for (int i = 1; i <= iSize; i++) {
			InodeBlock InodeBlock = new InodeBlock();
			for (int j = 0; j < InodeBlock.node.length; j++) {
				InodeBlock.node[j].flags = 0;
			}
			Disk.write(i, InodeBlock);
		}
		freeList = new LinkedList<Integer>();
		for (int i = iSize + 1; i < size; i++) {
			freeList.add(i);
		}

		return 0;

	}

	// Fecha todos os arquivos abertos e encerra o disco simulado.
	public int shutdown() {
		for (int i = 0; i < FileTable.fdArray.length - 1; i++) {
			if (FileTable.isValid(i)) {
				this.close(i);
			}
		}
		SuperBlock.freeList = this.freeList.size();
		Disk.stop();
		return 0;

	}

	// Cria um novo arquivo vazio.
	public int create() {
		for (int i = 1; i < SuperBlock.iSize; i++) {
			InodeBlock InodeBlock = new InodeBlock();
			Disk.read(i, InodeBlock);
			for (int j = 1; j < InodeBlock.node.length; j++) {
				Inode Inode = InodeBlock.node[j];
				if (Inode.flags == 0) {
					Inode.flags = 1;
					Inode.fileSize = 0;
					Inode.owner = 0;
					int fd = FileTable.allocate();
					if (fd != -1) {
						FileTable.add(Inode, iNumber_counter, fd);
						iNumber_counter++;
						return fd;
					}
				}
			}
		}
		return -1;

	}

	public int inumber(int fd) {
		if (!FileTable.isValid(fd))
			return -1;
		return FileTable.getInumber(fd) + 1;

	}

/*	public int open(int iNumber) {
		iNumber--;
		int blockNumber = (iNumber / IBLOCK_SIZE) + 1;
		InodeBlock InodeBlock = new InodeBlock();
		Disk.read(blockNumber, InodeBlock);
		Inode node = InodeBlock.node[iNumber % 8];
		int fd = FileTable.allocate();
		if (fd != -1) {
			FileTable.add(node, iNumber, fd);
		}
		return fd;
	}
*/
	public int open(int iNumber) 
	  {
	    for (int i=0; i<FileTable.fdArray.length; i++)   // check file for being opened
	    {
	        if (FileTable.fdArray[i] != null &&	     // an opened file entry
	            FileTable.fdArray[i].getInumber() == iNumber)
	          return(-1);                        // the file is already opened
	    }

	    InodeBlock ib = new InodeBlock();
	    for (int i=1; i<=SuperBlock.iSize; i++)          // loop over all inode blocks
	    {
	      Disk.read(i, ib);
	      for (int j=0; j<ib.node.length; j++)   // loop over inodes in the block
	      {
	        Inode inode = ib.node[j];
	        if (inode.flags != 0 && inode.owner == iNumber)  // file exists
	        {
	          int ind = FileTable.allocate();            // allocate place in FileDescr.  table
	          if (ind >= 0) 
	          {
	            FileDescriptor fd = new FileDescriptor(inode, iNumber);
//	            ft.bitmap[ind] = 1;               // make place as occupied
	            FileTable.fdArray[ind] = fd;
	            return(ind);
	          }
	    //      error("Too many opened files");
	          return(-1);
	        }
	      }
	    }
	  // error("File does not exist");
	    return(-1);
	  } 
	public int read(int fd, byte[] buffer) {
		if (!FileTable.isValid(fd))
			return -1;
		FileDescriptor fileDescriptor = FileTable.fdArray[fd];
		Inode Inode = fileDescriptor.getInode();
		if (Inode.fileSize > Disk.BLOCK_SIZE * 10) {
			byte[] directBuffer = new byte[Disk.BLOCK_SIZE * 10];
			int count = readFromDirectBlock(fileDescriptor, Inode, directBuffer);
			if (Inode.fileSize <= SINGLE_INDIRECTBLOCK_SIZE) {
				byte[] indirectBuffer = new byte[Inode.fileSize
						- (Disk.BLOCK_SIZE * 10)];
				readFromSingleIndirectBlock(fileDescriptor, Inode, buffer);
			}
		} else {
			return readFromDirectBlock(fileDescriptor, Inode, buffer);
		}
		return -1;
		
	} 

	public int write(int fd, byte[] buffer) {
		if (!FileTable.isValid(fd))
			return -1;
		FileDescriptor fileDescriptor = FileTable.fdArray[fd];
		Inode Inode = fileDescriptor.getInode();
		if (Inode.fileSize > 0) {
			
		}
		
		if (buffer.length > Disk.BLOCK_SIZE * 10) {
			byte[] directBuffer = new byte[Disk.BLOCK_SIZE * 10];
			int count = 0;
			for (int i = 0; i < Disk.BLOCK_SIZE * 10; i++) {
				directBuffer[i] = buffer[i];
				count++;
			}
			writeToDirectBlock(fileDescriptor, Inode, directBuffer);
			if (buffer.length <= SINGLE_INDIRECTBLOCK_SIZE) {
				byte[] indirectBuffer = new byte[buffer.length];
				for (int i = Disk.BLOCK_SIZE * 10; i < buffer.length; i++) {
					indirectBuffer[i] = buffer[i];
					count++;
				}
				writeToSingleIndirectBlock(fileDescriptor, Inode,
						indirectBuffer);
			}
			return count;
		} else {
			return writeToDirectBlock(fileDescriptor, Inode, buffer);
		}
	}

	// Modifica o ponteiro de busca.
	public int seek(int fd, int offset, int whence) {
		int seekPointer = FileTable.getSeekPointer(fd);
		switch (whence) {
		case SEEK_SET:
			seekPointer = offset;
			break;
		case SEEK_CUR:
			seekPointer += offset;
			break;
		case SEEK_END:
			seekPointer = FileTable.getInode(fd).fileSize + offset;
			break;
		default:
			seekPointer = -1;
		}
		if (seekPointer < 0)
			return -1;
		return seekPointer;
	}

	public int close(int fd) {
		if (!FileTable.isValid(fd))
			return -1;

		FileDescriptor fileDescriptor = FileTable.fdArray[fd];
		InodeBlock InodeBlock = new InodeBlock();
		int iNumber = FileTable.getInumber(fd);
		int blockNumber = (iNumber / IBLOCK_SIZE) + 1;
		Disk.read(blockNumber, InodeBlock);
		InodeBlock.node[iNumber % 8] = fileDescriptor.getInode();
		Disk.write(blockNumber, InodeBlock);
		FileTable.free(fd);
		return 0;

	}

	public int delete(int iNumber) {
		if (FileTable.isValid(FileTable.getFDfromInumber(iNumber)))
			return -1;
		int fd = FileTable.getFDfromInumber(iNumber);
		FileDescriptor fileDescriptor = FileTable.fdArray[fd];
		Inode Inode = fileDescriptor.getInode();
		for (int i = 0; i <= Inode.fileSize / Disk.BLOCK_SIZE; i++) {

		}
		throw new RuntimeException("");
	}

	public String toString() {
		throw new RuntimeException("");
	}

	public int writeToDirectBlock(FileDescriptor fileDescriptor, Inode Inode,
			byte[] buffer) {
		int pointerLoc = 0;
		int i = 0;

		Inode.pointer[pointerLoc] = freeList.removeFirst();

		Inode.fileSize = buffer.length;
		fileDescriptor.setSeekPointer(buffer.length);

		
		if (buffer.length <= 512) {
			
			byte[] buf = new byte[Disk.BLOCK_SIZE];
			for (i = 0; i < buffer.length; i++) {
				buf[i] = buffer[i];
			}
			
			Disk.write(Inode.pointer[pointerLoc], buf);
			return i;
		} else {
			int writes = 0;
			int count = 0;
			for (int j = 0; j < buffer.length; j += 512) {
				byte[] buf = new byte[Disk.BLOCK_SIZE];
				if (buffer.length - j >= Disk.BLOCK_SIZE) {
					for (i = 0; i < Disk.BLOCK_SIZE; i++) {
						buf[i] = buffer[i + j];
						writes++;
					}
				} else {
					for (i = 0; i < buffer.length - j; i++) {
						buf[i] = buffer[i + j];
						writes++;
					}
				}
				Disk.write(Inode.pointer[count], buf);
				count++;
			}
			return writes;
		}
	}

	public int writeToSingleIndirectBlock(FileDescriptor fileDescriptor,
			Inode Inode, byte[] buffer) {
		int pointerLoc = 10;
		int i = 0;
		
		IndirectBlock IndirectBlock = new IndirectBlock();
		Inode.pointer[pointerLoc] = IndirectBlock.pointer[0];
		
		int count = 0;
		int writes = 0;
		for (int j = 0; j <= buffer.length; j += 512) {
			byte[] buf = new byte[Disk.BLOCK_SIZE];
			if (buffer.length - j >= Disk.BLOCK_SIZE) {
				for (i = 0; i < Disk.BLOCK_SIZE; i++) {
					buf[i] = buffer[i + j];
					writes++;
				}
			} else {
				for (i = 0; i < buffer.length - j; i++) {
					buf[i] = buffer[i + j];
					writes++;
				}
			}
			Disk.write(IndirectBlock.pointer[count], buf);
			count++;
		}
		
		Inode.fileSize = buffer.length;
		fileDescriptor.setSeekPointer(buffer.length);
		
		Disk.write(Inode.pointer[pointerLoc], IndirectBlock);
		return writes;
	}

	public int writeToDoubleIndirectBlock(FileDescriptor fileDescriptor,
			Inode Inode, byte[] buffer) {
		int pointerLoc = 11;
		return 0;
	}

	public int writeToTripleIndirectBlock(FileDescriptor fileDescriptor,
			Inode Inode, byte[] buffer) {
		int pointerLoc = 12;
		return 0;
	}

	public int readFromDirectBlock(FileDescriptor fileDescriptor, Inode Inode,
			byte[] buffer) {
		int pointerLoc = 0;
		byte[] buf = new byte[Disk.BLOCK_SIZE];
		Disk.read(Inode.pointer[pointerLoc], buf);
		for (int i = fileDescriptor.getSeekPointer(); i < buffer.length; i++) {
			buffer[i] = buf[i];
		}
		return Inode.fileSize;
	}

	public int readFromSingleIndirectBlock(FileDescriptor fileDescriptor,
			Inode Inode, byte[] buffer) {
		int pointerLoc = 10;

		IndirectBlock IndirectBlock = new IndirectBlock();
		Disk.read(Inode.pointer[pointerLoc], IndirectBlock);
		int count = 0;
		int reads = 0;
		for (int i = fileDescriptor.getSeekPointer(); i < Inode.fileSize; i += 512) {
			byte[] buf = new byte[Disk.BLOCK_SIZE];
			for (int j = i; j < Disk.BLOCK_SIZE; j++) {
				buf[i] = buffer[i + j];
				reads++;
			}
			Disk.read(IndirectBlock.pointer[count], buf);
			count++;
		}
		return reads;
	}

	public int readFromDoubleIndirectBlock(FileDescriptor fileDescriptor,
			Inode Inode, byte[] buffer) {
		int pointerLoc = 11;
		return -1;
	}

	public int readFromTripleIndirectBlock(FileDescriptor fileDescriptor,
			Inode Inode, byte[] buffer) {
		int pointerLoc = 12;
		return -1;
	}
}