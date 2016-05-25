import java.io.*;
import java.security.*;
import java.lang.reflect.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class MyClassLoader extends ClassLoader {
	// 这些对象在构造函数中设置，
	// 以后loadClass()方法将利用它们解密类
	private SecretKey key;
	private Cipher cipher;

	// 设置解密所需要的对象
	public MyClassLoader(SecretKey key) throws GeneralSecurityException, IOException {
		this.key = key;

		String algorithm = "DES";
		SecureRandom sr = new SecureRandom();
		System.err.println("[MyClassLoader: creating cipher]");
		cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, sr);
	}

	// main过程：读入密匙，创建MyClassLoader的
	// 实例，它就是我们的定制ClassLoader。
	// 设置好ClassLoader以后，用它装入应用实例，
	// 最后，通过Java Reflection API调用应用实例的main方法
	static public void main(String args[]) throws Exception {
		String keyFilename = args[0];
		String appName = args[1];

		// 这些是传递给应用本身的参数
		String realArgs[] = new String[args.length - 2];
		System.arraycopy(args, 2, realArgs, 0, args.length - 2);

		// 读取密匙
		System.err.println("[MyClassLoader: reading key]");
		byte rawKey[] = FileUtil.readFile(keyFilename);
		DESKeySpec dks = new DESKeySpec(rawKey);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);

		// 创建解密的ClassLoader
		MyClassLoader dr = new MyClassLoader(key);

		// 创建应用主类的一个实例
		// 通过ClassLoader装入它
		System.err.println("[MyClassLoader: loading " + appName + "]");
		Class clasz = dr.loadClass(appName);

		// 最后，通过Reflection API调用应用实例的main()方法

		// 获取一个对main()的引用
		String proto[] = new String[1];
		Class mainArgs[] = { (new String[1]).getClass() };
		Method main = clasz.getMethod("main", mainArgs);

		// 创建一个包含main()方法参数的数组
		Object argsArray[] = { realArgs };
		System.err.println("[MyClassLoader: running " + appName + ".main()]");

		// 调用main()
		main.invoke(null, argsArray);
	}

	public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			// 要创建的Class对象
			Class clasz = null;

			// 如果类已经在系统缓冲之中,不必再次装入它
			clasz = findLoadedClass(name);

			if (clasz != null)
				return clasz;

			// 下面是定制部分
			try {
				// 读取经过加密的类文件
				byte classData[] = FileUtil.readFile(name + ".class");

				if (classData != null) {
					// 解密...
					byte decryptedClassData[] = cipher.doFinal(classData);

					// ... 再把它转换成一个类
					clasz = defineClass(name.replace('/', '.'), decryptedClassData, 0, decryptedClassData.length);
					System.err.println("[MyClassLoader: decrypting class " + name + "]");
				}
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			}

			// 如果上面没有成功尝试用默认的ClassLoader装入它
			if (clasz == null)
				clasz = findSystemClass(name);

			// 如有必要，则装入相关的类
			if (resolve && clasz != null)
				resolveClass(clasz);

			// 把类返回给调用者
			return clasz;
		} catch (IOException ie) {
			throw new ClassNotFoundException(ie.toString());
		} catch (GeneralSecurityException gse) {
			throw new ClassNotFoundException(gse.toString());
		}
	}
}
