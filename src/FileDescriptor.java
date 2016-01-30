
class FileDescriptor
{    
    private Inode inode;
    int inumber;
    private int seekptr;
    
    public FileDescriptor(Inode inode, int inumber){
		this.inode = inode;
		this.inumber = inumber;
		seekptr = 0;
    }
    
    public Inode getInode(){
		return inode;
    }
    
    public int getInumber(){
		return inumber;
    }
    
    public int getSeekPointer(){
		return seekptr;
    }

    public void setSeekPointer(int i){
		seekptr = i;
    }

    public void setFileSize(int newSize){
		inode.fileSize = newSize;
    }
	
}
