package trie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * <p>
 * Description: 测试
 * </p>
 * 
 * @author junjin4838
 * @date 2017年1月22日
 * @version 1.0
 */
public class Sample {

	private Trie mTrie;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Sample sample = new Sample();
		sample.mTrie = new Trie(false);
		sample.init();
		
		System.out.println("这个Trie树有" + sample.mTrie.size() + "个不同的单词！");
		
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String s = scanner.nextLine();
			System.out.println("出现次数为：" + sample.getCount(s));
		}
	}

	/**
	 * 初始化文件，将文本目录添加到字典树
	 */
	private void init() {
		try {
			InputStream in = new FileInputStream(new File("/Users/junjin4838/git/trieTree/bin/resource/word.txt"));
			addToDictionary(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**	
     * 从输入流中读取数据加载到字典树
     * @param f
     * @throws IOException
     * @throws FileNotFoundException
     */
	public void addToDictionary(InputStream f) throws IOException,FileNotFoundException {
		long t = System.currentTimeMillis();
		final int bufSize = 1000;
		int read;
		int numWords = 0;
		InputStreamReader fr = null;
		try {
			fr = new InputStreamReader(f);
			char[] buf = new char[bufSize];
			while ((read = fr.read(buf)) != -1) {
				String[] words = new String(buf, 0, read).split("\\W");
				for (String s : words) {
					mTrie.insert(s);
					numWords++;
				}
			}
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
				}
			}
		}
		System.out.println("Read from file and inserted " + numWords
				+ " words into trie in " + (System.currentTimeMillis() - t)
				/ 1000.0 + " seconds.");
	}

	public int getSize() {
		if (mTrie != null) {
			return mTrie.size();
		}
		return 0;
	}

	/**
	 * 字符串存在的个数
	 * @param s
	 * @return
	 */
	public int getCount(String s) {
		return mTrie.frequency(s);
	}

}
