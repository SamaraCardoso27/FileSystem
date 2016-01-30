
interface FileSystem {

    public int formatDisk(int size, int iSize);   
    public int shutdown();     
    public int create();    
    public int inumber(int fd);       
    public int open(int iNumber);        
    public int read(int fd, byte[] buffer); 
    public int write(int fd, byte[] buffer); 
    public int seek(int fd, int offset, int whence);     
    public int close(int fd); 
    public int delete(int iNumber); 
}



