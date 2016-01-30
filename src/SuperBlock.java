class SuperBlock {
	int size; 
	int iSize; 
	int freeList;	
	public String toString () {
		return
			"SUPERBLOCK:\n"
			+ "Size: " + size
			+ "  Isize: " + iSize
			+ "  freeList: " + freeList;
	}
}