# DiskLruCacheHelper
a helper class for DiskLruCache(https://github.com/JakeWharton/DiskLruCache)


you can create DiskLruCache more easily by using DiskLruCacheHelper.
what's more,it providers a series of static method to read/write/asynchronous write/remove cache.
for example,if you want to write cache to Bitmap,just call DiskLruCacheHelper.writeBitmapToCache(),never need to call DiskLruCache.edit()、commit() and so on.there are some another useful methods like getDiskCacheDir()、getAppVersion（）...

##in a word,mom won't worry about my study when using DiskLruCacheHelper:)

