package com.jakewharton.disklrucache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

/**
 * @author Rowandjj
 *
 *DiskLruCache�ĸ���������(DiskLruCache:http://jakewharton.github.io/DiskLruCache,https://github.com/JakeWharton/DiskLruCache)
 * �ṩ����/��ȡ/д�뻺��ķ���,��֧���첽����д��.
 */
public class DiskLruCacheHelper
{
	private static final long DEFAULT_MAX_SIZE = 10*1024*1024;
	
	private static ExecutorService service = null;
	/**
	 * 
	 * ����DiskLruCacheʵ��,Ĭ�ϰ汾��Ϊ��ǰӦ�ð汾�ţ�����λ����getDiskCacheDirָ��
	 * @param context ������
	 * @param cacheDirName �����ļ�������
	 * @param maxSize �������ֵ,��λ��byte
	 * @return �����ɹ�����DiskLruCacheʵ�����򷵻�null
	 */
	public static DiskLruCache createCache(Context context,String cacheDirName,long maxSize)
	{
		DiskLruCache cache = null;
		try
		{
			 cache = DiskLruCache.open(getDiskCacheDir(cacheDirName, context), getAppVersion(context),1, maxSize);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return cache;
	}
	/**
	 * ����һ������Ĭ�ϴ�С��DiskLruCacheʵ��,Ĭ�ϴ�СΪ10Mb
	 * @param context
	 * @param cacheDirName
	 * @return �����ɹ�����DiskLruCacheʵ�����򷵻�null
	 */
	public static DiskLruCache createCache(Context context,String cacheDirName)
	{
		return createCache(context, cacheDirName, DEFAULT_MAX_SIZE);
	}
	
	/**
	 * ��ͼƬд�뻺��
	 */
	public static boolean writeBitmapToCache(DiskLruCache cache,Bitmap bitmap,String url)
	{
		return writeBitmapToCache(cache, bitmap, url, CompressFormat.JPEG,100);
	}
	
	/**
	 * �첽�ؽ�ͼƬд�뻺�档�����᲻��д������
	 * @param cache
	 * @param bitmap
	 * @param url
	 */
	public static void asyncWriteBitmapToCache(final DiskLruCache cache,final Bitmap bitmap,final String url)
	{
		if(service == null)
			service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				writeBitmapToCache(cache, bitmap, url);
			}
		});
	}
	
	/**
	 * ��ͼƬд�뻺��
	 * @param cache �������
	 * @param bitmap ͼƬ����
	 * @param url ���ڱ�ʶbitmap��Ψһ���ƣ�ͨ��ΪͼƬurl
	 * @return true��ʾд�뻺��ɹ�����Ϊfalse
	 */
	public static boolean writeBitmapToCache(DiskLruCache cache,Bitmap bitmap,String url,CompressFormat format, int quality)
	{
		if(cache == null || bitmap == null || url == null || TextUtils.isEmpty(url))
			return false;
		try
		{
			DiskLruCache.Editor editor = cache.edit(generateKey(url));
			if(editor != null)
			{
				OutputStream out = editor.newOutputStream(0);
				if(bitmap.compress(format,quality, out))
				{
					editor.commit();
					return true;
				}					
				else
				{
					editor.abort();
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * �첽�ؽ�ͼƬд�뻺�档�����᲻��д������
	 */
	public static void asyncWriteBitmapToCache(final DiskLruCache cache,final Bitmap bitmap,final String url,final CompressFormat format, final int quality)
	{
		if(service == null)
			service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				writeBitmapToCache(cache, bitmap, url,format,quality);
			}
		});
	}
	
	/**
	 * �첽�ؽ�inputStram��д�뻺�棬�����᷵��д������
	 */
	public static void asyncWriteStreamToCache(final DiskLruCache cache,final InputStream in,final String url)
	{
		if(service == null)
			service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				asyncWriteStreamToCache(cache, in, url);
			}
		});
	}
	
	/**
	 * ��inputStram��д�뻺��
	 * @param cache
	 * @param in
	 * @param url
	 * @return
	 */
	public static boolean writeStreamToCache(DiskLruCache cache,InputStream in,String url)
	{
		if(cache == null || in == null || url == null|| TextUtils.isEmpty(url))
			return false;
		DiskLruCache.Editor editor = null;
		try
		{
			editor = cache.edit(generateKey(url));
			if(editor != null)
			{
				OutputStream out = editor.newOutputStream(0);
				BufferedInputStream bin = new BufferedInputStream(in);
				byte[] buffer = new byte[1024];
				int len = 0;
				while((len = bin.read(buffer)) != -1)
				{
					out.write(buffer, 0, len);
					out.flush();
				}
				editor.commit();
				return true;
			}
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * �첽�ؽ��ļ�д�뻺�棬�����᷵��д������
	 */
	public static void asyncWriteFileToCache(final DiskLruCache cache,final File file,final String url)
	{
		if(service == null)
			service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				writeFileToCache(cache, file, url);
			}
		});
	}
	/**
	 * ���ļ�д�뻺��
	 * @return true��ʾд��ɹ�����д��ʧ��
	 */
	public static boolean writeFileToCache(DiskLruCache cache,File file,String url)
	{
		if(cache == null || file == null || url == null || !file.exists() || TextUtils.isEmpty(url))
		{
			return false;
		}
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return writeStreamToCache(cache, fin, url);
	}
	
	/**
	 * �첽�ؽ��ַ���д�뻺��,�����᷵��д����
	 */
	public static void asyncWriteStringToCache(final DiskLruCache cache,final String str,final String url)
	{
		if(service == null)
			service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable()
		{
			@Override
			public void run()
			{
				writeStringToCache(cache, str, url);
			}
		});
	}
	/**
	 * ���ַ���д�뻺��
	 * @param cache
	 * @param str
	 * @param url
	 * @return
	 */
	public static boolean writeStringToCache(DiskLruCache cache,String str,String url)
	{
		if(cache == null || str == null || url == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(str))
		{
			return false;
		}
		DiskLruCache.Editor editor = null;
		try
		{
			editor = cache.edit(generateKey(url));
			if(editor != null)
			{
				OutputStream out = editor.newOutputStream(0);
				out.write(str.getBytes());
				out.flush();
			}
			editor.commit();
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	
	/**
	 * ֹͣ�ڲ�����д������̣߳�
	 * �⽫���²���д���������ܽ��С�
	 */
	public static void stop()
	{
		if(service != null)
			service.shutdownNow();
	}
	
	/**
	 * 
	 * ����url��ȡ���棬���������String��ʽ����
	 * @param cache
	 * @param url
	 * @return �ɹ��򷵻�String���򷵻�null
	 */
	public static String readCacheToString(DiskLruCache cache,String url)
	{
		if(cache == null || url == null || TextUtils.isEmpty(url))
			return null;
		String key = generateKey(url);
		DiskLruCache.Snapshot snapshot = null;
		try
		{
			snapshot = cache.get(key);
			if(snapshot != null)
			{
				InputStream in = snapshot.getInputStream(0);
				StringBuilder builder = new StringBuilder(1024*2);
				int len = 0;
				byte[] buffer = new byte[1024];
				while((len = in.read(buffer)) != -1)
				{
					builder.append(new String(buffer,0,len));
				}
				return builder.toString();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * ����url��ȡ���棬����������InputStream��ʽ����
	 * 
	 * @param cache DiskLruCacheʵ��
	 * @param url ������
	 * @return �����򷵻�InputStream�����򷵻�null
	 */
	public static InputStream readCacheToInputStream(DiskLruCache cache,String url)
	{
		if(cache == null || url == null || TextUtils.isEmpty(url))
			return null;
		String key = generateKey(url);
		DiskLruCache.Snapshot snapshot = null;
		try
		{
			snapshot = cache.get(key);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if(snapshot != null)
			return snapshot.getInputStream(0);
		return null;
	}
	
	
	/**
	 * ����url��ȡ���棬����������Bitmap��ʽ����
	 * @param cache
	 * @param url
	 * @return �ɹ�����bitmap�����򷵻�null
	 */
	public static Bitmap readCacheToBitmap(DiskLruCache cache,String url)
	{
		InputStream in = readCacheToInputStream(cache, url);
		if(in != null)
			return BitmapFactory.decodeStream(in);
		return null;
	}
	
	/**
	 * ��ȡ�����ļ�·��(����ѡ��sd��)
	 * @param cacheDirName �����ļ�������
	 * @param context ������
	 * @return
	 */
	public static File getDiskCacheDir(String cacheDirName,Context context)
	{
		String cacheDir;
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)&&!Environment.isExternalStorageRemovable())
		{
			cacheDir = getExternalCacheDir(context);
			if(cacheDir == null)//���ֻ��ͷ�����null
				cacheDir = getInternalCacheDir(context);
		}else
		{
			cacheDir = getInternalCacheDir(context);
		}
		File dir =  new File(cacheDir,cacheDirName);
		if(!dir.exists())
			dir.mkdirs();
		return dir;
	}
	
	/**
	 * ��ȡ��ǰapp�汾��
	 * @param context ������
	 * @return ��ǰapp�汾��
	 */
	public static int getAppVersion(Context context)
	{
		PackageManager manager = context.getPackageManager();
		int code = 1;
		try
		{
			code = manager.getPackageInfo(context.getPackageName(),0).versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return code;
	}
	
	
	/**
	 * ����ָ����url�Ƴ�ָ������
	 * Note:�벻Ҫ��ʹ��DiskLruCache.remove()
	 * 
	 * @param cache
	 * @param url
	 * @return
	 */
	public static boolean remove(DiskLruCache cache,String url)
	{
		if(cache == null || url == null || TextUtils.isEmpty(url))
		{
			return false;
		}
		try
		{
			return cache.remove(generateKey(url));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * ����ԭʼ�������¼����Ա�֤�������ƵĺϷ���
	 * @param key ԭʼ����ͨ����url
	 * @return
	 */
	public static String generateKey(String key)
	{
		String cacheKey;
		try
		{
			MessageDigest digest = MessageDigest.getInstance("md5");
			digest.update(key.getBytes());
			cacheKey = bytesToHexString(digest.digest());
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			cacheKey = String.valueOf(key.hashCode());  
		}
		return cacheKey;
	}
	
	private static String bytesToHexString(byte[] bytes)
	{
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < bytes.length; i++)
		{
			String hex = Integer.toHexString(0xff&bytes[i]);
			if(hex.length() == 1)
				builder.append('0');
			builder.append(hex);
		}
		return builder.toString();
	}
	
	private static String getExternalCacheDir(Context context)
	{
		File dir = context.getExternalCacheDir();
		if(dir == null)
			return null;
		if(!dir.exists())
			dir.mkdirs();
		return dir.getPath();
	}
	
	private static String getInternalCacheDir(Context context)
	{
		File dir = context.getCacheDir();
		if(!dir.exists())
			dir.mkdirs();
		return dir.getPath();
	}
}
