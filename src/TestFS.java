/**
Professor esse são alguns teste para que possa fazer o teste com o sistema de Arquivo

Teste para sistema de Arquivo - Projeto de SO


-->  formatDisk 100 10
--> file1 = create
--> inum1 = inumber file1
--> write file1 HiThere 10000
--> seek file1 -10 1
--> read file1 50
--> file2 = create
--> inum2 = inumber file2
--> write file2 Names_ 512
--> file3 = create
--> inum3 = inumber file3
--> file2 = open inum1
--> close file1
--> delete inum2
--> shutdown
--> quit

Teste com Verificação de Erro

--> formatDisk 100 10
--> file1 = create
--> open 21
--> close 21
--> close -1
--> write file1 Projeto de SO 2048
--> write file1 SO 2048
--> write file4 Aaaaaayyyyy!_ 512
--> read file2 100
--> close file2
--> inum1 = inumber file2
--> shutdown
--> QUIT
 */


import java.io.*;
import java.util.*;

class TestFS {

	private static FileSystem fs = new JavaFileSystem();
	private static Hashtable vars = new Hashtable();

	public static void main(String[] args) {
		if (args.length > 1)
			System.err.println("Uso: Testes [arquivo]");

		boolean fromFile = (args.length == 1);

		BufferedReader data = null;

		if (fromFile) {
			try {
				data = new BufferedReader(new FileReader(new File(args[0])));
			} catch (FileNotFoundException e) {
				System.err.println("Erro: Arquivo " + args[0]
						+ " não encontrado.");
				System.exit(1);
			}
		} else
			data = new BufferedReader(new InputStreamReader(System.in));

		for (;;) {
			try {

				if (!fromFile) {
					System.out.print("--> ");
					System.out.flush();
				}

				String line = data.readLine();

				if (line == null)
					System.exit(0);
				line = line.trim();
				if (line.length() == 0) {
					System.out.println();
					continue;
				}

				if (line.startsWith("//")) {
					if (fromFile)
						System.out.println(line);
					continue;
				}
				if (line.startsWith("/*"))
					continue;
				if (fromFile)
					System.out.println("--> " + line);

				String target = null;
				int equals = line.indexOf('=');
				if (equals > 0) {
					target = line.substring(0, equals).trim();
					line = line.substring(equals + 1).trim();
				}

				StringTokenizer cmds = new StringTokenizer(line);
				String cmd = cmds.nextToken();

				int result = 0;
				if (cmd.equalsIgnoreCase("formatDisk")
						|| cmd.equalsIgnoreCase("format")) {
					int arg1 = nextValue(cmds);
					int arg2 = nextValue(cmds);
					result = fs.formatDisk(arg1, arg2);
				} else if (cmd.equalsIgnoreCase("shutdown")) {
					result = fs.shutdown();
				} else if (cmd.equalsIgnoreCase("create")) {
					result = fs.create();
				} else if (cmd.equalsIgnoreCase("open")) {
					result = fs.open(nextValue(cmds));
				} else if (cmd.equalsIgnoreCase("inumber")) {
					result = fs.inumber(nextValue(cmds));
				} else if (cmd.equalsIgnoreCase("read")) {
					int arg1 = nextValue(cmds);
					int arg2 = nextValue(cmds);
					result = readTest(arg1, arg2);
				} else if (cmd.equalsIgnoreCase("write")) {
					int arg1 = nextValue(cmds);
					String arg2 = cmds.nextToken();
					int arg3 = nextValue(cmds);
					result = writeTest(arg1, arg2, arg3);
				} else if (cmd.equalsIgnoreCase("seek")) {
					int arg1 = nextValue(cmds);
					int arg2 = nextValue(cmds);
					int arg3 = nextValue(cmds);
					result = fs.seek(arg1, arg2, arg3);
				} else if (cmd.equalsIgnoreCase("close")) {
					result = fs.close(nextValue(cmds));
				} else if (cmd.equalsIgnoreCase("delete")) {
					result = fs.delete(nextValue(cmds));
				} else if (cmd.equalsIgnoreCase("quit")) {
					System.exit(0);
				} else if (cmd.equalsIgnoreCase("vars")) {
					for (Enumeration e = vars.keys(); e.hasMoreElements();) {
						Object key = e.nextElement();
						Object val = vars.get(key);
						System.out.println("\t" + key + " = " + val);
					}
					continue;
				} else {
					System.out.println("Comando inválido");
					continue;
				}

				if (target == null)
					System.out.println("    Resultado: " + result);
				else {
					vars.put(target, new Integer(result));
					System.out.println("    " + target + " = " + result);
				}
			}

			catch (NumberFormatException e) {
				System.out.println("Argumento Incorreto");
			} catch (NoSuchElementException e) {
				System.out.println("Número incorreto de elementos");
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	static private int nextValue(StringTokenizer cmds) {
		String arg = cmds.nextToken();
		Object val = vars.get(arg);
		return (val == null) ? Integer.parseInt(arg) : ((Integer) val)
				.intValue();
	}

	private static int readTest(int fd, int size) {
		byte[] buffer = new byte[size];
		int length;

		for (int i = 0; i < size; i++)
			buffer[i] = (byte) '*';
		length = fs.read(fd, buffer);
		for (int i = 0; i < length; i++)
			showchar(buffer[i]);
		if (length != -1)
			System.out.println();
		return length;
	}

	private static int writeTest(int fd, String str, int size) {
		byte[] buffer = new byte[size];

		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte) str.charAt(i % str.length());

		return fs.write(fd, buffer);
	}

	private static void showchar(byte b) {
		if (b < 0) {
			System.out.print("M-");
			b += 0x80;
		}
		if (b >= ' ' && b <= '~') {
			System.out.print((char) b);
			return;
		}
		switch (b) {
		case '\0':
			System.out.print("\\0");
			return;
		case '\n':
			System.out.print("\\n");
			return;
		case '\r':
			System.out.print("\\r");
			return;
		case '\b':
			System.out.print("\\b");
			return;
		case 0x7f:
			System.out.print("\\?");
			return;
		default:
			System.out.print("^" + (char) (b + '@'));
			return;
		}
	}
}