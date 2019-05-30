/*
* author ming
* date 2019-05-30
* 为了兼容oss   所有分页 和 分类和归档 全部增加index.html   避免 oss给自动转发
* */
hexo.extend.helper.register('paginator', require('./ming-paginator'));
hexo.extend.helper.register('list_categories', require('./ming_categories'));
hexo.extend.helper.register('list_archives', require('./ming_archives'));
hexo.extend.helper.register('list_tags', require('./ming_tags'));
hexo.extend.helper.register('list_posts', require('./ming_posts'));
hexo.extend.helper.register('tag_cloud', require('./ming_tagcloud'));
hexo.extend.helper.register('tagcloud', require('./ming_tagcloud'));


