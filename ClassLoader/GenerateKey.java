// GenerateKey.java
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class GenerateKey {
	static public void main(String args[]) throws Exception {
		String keyFilename = args[0];
		// 生成密匙
		SecureRandom sr = new SecureRandom();
		KeyGenerator kg = KeyGenerator.getInstance("DES");
		kg.init(sr);
		SecretKey key = kg.generateKey();

		// 把密匙数据保存到文件
		FileUtil.writeFile(keyFilename, key.getEncoded());
	}
}
