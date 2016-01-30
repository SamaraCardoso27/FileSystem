class Inode {
	// tamanho de um inode em bytes
	public final static int SIZE = 64; 
	int flags;
	int owner;
	int fileSize;
	public int pointer[] = new int[13];

	public String toString() {
		String s = "[Flags: " + flags + "  Size: " + fileSize + "  ";
		for (int i = 0; i < 13; i++)
			s += "|" + pointer[i];
		return s + "]";
	}
}