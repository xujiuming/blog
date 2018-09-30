#!/usr/bin/env groovy
@GrabResolver(name = 'aliyun', root = 'http://maven.aliyun.com/nexus/content/groups/public/')
//设定本脚本grape 依赖
@Grapes([
        @Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-core', version = '4.1.0'),
        @Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-cdn', version = '2.9.0')
])
import com.aliyuncs.DefaultAcsClient
@GrabResolver(name = 'aliyun', root = 'http://maven.aliyun.com/nexus/content/groups/public/')
//设定本脚本grape 依赖
@Grapes([
        @Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-core', version = '4.1.0'),
        @Grab(group = 'com.aliyun', module = 'aliyun-java-sdk-cdn', version = '2.9.0')
])

/**基于groovy 使用 aliyun sdk 刷新cdn
 *
 * @author ming
 * @date 2018-09-30 09:10:44
 */
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.IAcsClient
import com.aliyuncs.cdn.model.v20141111.RefreshObjectCachesRequest
import com.aliyuncs.cdn.model.v20141111.RefreshObjectCachesResponse
import com.aliyuncs.exceptions.ClientException
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.profile.IClientProfile

String OBJECT_TYPE_DIRECTORY = "Directory"

RefreshObjectCachesRequest describe = new RefreshObjectCachesRequest()
describe.setObjectPath("https://xujiuming.com/\nhttps://www.xujiuming.com/\nhttps://blog.xujiuming.com/")
describe.setObjectType(OBJECT_TYPE_DIRECTORY)
IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", ACCESS_KEY_ID, ACCESS_KEY_VALUE)
IAcsClient client = new DefaultAcsClient(profile)
try {
    RefreshObjectCachesResponse response = client.getAcsResponse(describe)
    return response
} catch (ClientException e) {
    e.printStackTrace()
} catch (Exception e) {
    e.printStackTrace()
}


