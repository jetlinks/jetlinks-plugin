package org.jetlinks.plugin.example.sdk.hc;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.plugin.example.sdk.LibUtils;
import org.jetlinks.plugin.internal.media.MediaSdkChannelConfig;
import org.jetlinks.plugin.internal.media.MediaSdkDeviceControl;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.sun.jna.Platform.isLinux;
import static com.sun.jna.Platform.isWindows;
import static org.jetlinks.plugin.example.sdk.hc.HCNetSDK.*;

/**
 * @create 2020-07-27-10:42
 */
@Slf4j
public class NetSDKDemo {
    static HCNetSDK hCNetSDK = null;
    static int lUserID = -1; //用户句柄
    public static flowTestcallback flowcallback;
    public static dev_work_state_cb workStateCb;
    public static FExceptionCallBack_Imp fExceptionCallBack;
    public static Map<Integer, HCNetSDK.NET_DVR_ALARM_RS485CFG> rs485cfg = new HashMap<>();
    public static Map<Integer, HCNetSDK.NET_DVR_PTZPOS> ptzcfg = new HashMap<>();


    static class dev_work_state_cb implements HCNetSDK.DEV_WORK_STATE_CB {
        public boolean invoke(Pointer pUserdata,
                              int iUserID,
                              HCNetSDK.NET_DVR_WORKSTATE_V40 lpWorkState) {

            lpWorkState.read();
            System.out.println("设备状态:" + lpWorkState.dwDeviceStatic);
            for (int i = 0; i < HCNetSDK.MAX_CHANNUM_V40; i++) {
                int channel = i + 1;
                System.out.println("第" + channel + "通道是否在录像：" + lpWorkState.struChanStatic[i].byRecordStatic);
            }
            return true;
        }

    }

    static class FExceptionCallBack_Imp implements HCNetSDK.FExceptionCallBack {
        public void invoke(int dwType,
                           int lUserID,
                           int lHandle,
                           Pointer pUser) {
            System.out.println("异常事件类型:" + dwType);
            return;
        }
    }

    static class flowTestcallback implements HCNetSDK.FLOWTESTCALLBACK {
        public void invoke(int lFlowHandle,
                           HCNetSDK.NET_DVR_FLOW_INFO pFlowInfo,
                           Pointer pUser) {
            pFlowInfo.read();
            System.out.println("发送的流量数据：" + pFlowInfo.dwSendFlowSize);
            System.out.println("接收的流量数据：" + pFlowInfo.dwRecvFlowSize);
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        NetSDKDemo sdk = new NetSDKDemo();
        sdk.initSDKInstance();

        Tuple2<Integer, HCNetSDK.NET_DVR_DEVICEINFO_V40> loginResult = sdk.Login_V40(
                "192.168.33.157", (short) 8000, "admin", "p@ssw0rd"
        );
        Integer userId = loginResult.getT1();
        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceinfo = loginResult.getT2();
        boolean online = sdk.getDeviceStatus(userId);
        System.out.println("在线：" + online);
        int channel = deviceinfo.struDeviceV30.byChanNum;

        List<HCNetSDK.NET_DVR_PRESET_NAME> presetNames = sdk.queryPreset(userId);
        for (HCNetSDK.NET_DVR_PRESET_NAME data : presetNames) {
            log.info("preset: {}, {}", String.valueOf(data.wPresetNum), new String(data.byName, "gbk"));
        }

        System.out.println(sdk.startRecord(userId, channel));
        Thread.sleep(1000);
        System.out.println(sdk.stopRecord(userId, channel));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<NET_DVR_FINDDATA_V30> records = sdk
                .queryRecord(
                        userId,
                        channel,
                        LocalDateTime.parse("2023-11-03 00:00:00", formatter),
                        LocalDateTime.parse("2023-11-03 23:59:59", formatter));
        log.info("录像数量：{}", records.size());

        int rtspPort = sdk.queryRtspPort(userId);
        log.info("rtsp port: {}", rtspPort);

        HCNetSDK.NET_DVR_ACCESS_DEVICE_CHANNEL_INFO channelInfo = sdk.queryChannel(
                userId,
                deviceinfo.struDeviceV30.byChanNum,
                "192.168.33.157",
                "p@ssw0rd",
                "admin",
                (short) 8000
        );
        log.info("通道数量：{}", channelInfo.dwTotalChannelNum + 1);

//        LinkedList<Integer> channelNo = sdk.createDeviceTreeV40(userInfo.getUserId(), userInfo.getDeviceInfo());
//        log.info("channel: {}", channelNo);
    }

//    public static void main(String[] args) throws IOException, InterruptedException {
//        if (hCNetSDK == null) {
//            if (!mockSDKInstance()) {
//                System.out.println("Load SDK fail");
//                return;
//            }
//        }
//        //linux系统建议调用以下接口加载组件库
//        if (isLinux()) {
//            HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
//            HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
//            //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
//            String strPath1 = System.getProperty("user.dir") + "/lib/libcrypto.so.1.1";
//            String strPath2 = System.getProperty("user.dir") + "/lib/libssl.so.1.1";
//
//            System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
//            ptrByteArray1.write();
//            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());
//
//            System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
//            ptrByteArray2.write();
//            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());
//
//            String strPathCom = System.getProperty("user.dir") + "/lib";
//            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
//            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
//            struComPath.write();
//            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
//        }
//
//        //SDK初始化，一个程序进程只需要调用一次
//        hCNetSDK.NET_DVR_Init();
//
//        if (fExceptionCallBack == null) {
//            fExceptionCallBack = new FExceptionCallBack_Imp();
//        }
//        Pointer pUser = null;
//        if (!hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, fExceptionCallBack, pUser)) {
//            return;
//        }
//        System.out.println("设置异常消息回调成功");
//
//        //启用SDK写日志
//        hCNetSDK.NET_DVR_SetLogToFile(3, "..\\sdkLog", false);
//
//        //登录设备，每一台设备只需要登录一次
//        lUserID = TestDemo.Login_V40("127.0.0.1", (short) 8000, "admin", "abcd1234");
//
//        TestDemo.SetPTZcfg(lUserID);
//        TestDemo.getAESInfo(lUserID);
//        TestDemo.getRS485Cfg(lUserID);
//        TestDemo.getRS485SlotInfo(lUserID);
//        TestDemo.getIPChannelInfo(lUserID);
//
////        Thread.sleep(2000);
//
//        //程序退出的时候调用注销登录接口，每一台设备分别调用一次
//        if (hCNetSDK.NET_DVR_Logout(lUserID)) {
//            System.out.println("注销成功");
//        }
//
//        //释放SDK资源，程序退出时调用，只需要调用一次
//        hCNetSDK.NET_DVR_Cleanup();
//        return;
//    }

    /**
     * 设备登录V30
     *
     * @param ip   设备IP
     * @param port SDK端口，默认设备的8000端口
     * @param user 设备用户名
     * @param psw  设备密码
     */
    public static int Login_V30(String ip,
                                short port,
                                String user,
                                String psw) {
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iUserID = hCNetSDK.NET_DVR_Login_V30(ip, port, user, psw, m_strDeviceInfo);
        System.out.println("UsID:" + lUserID);
        if ((iUserID == -1) || (iUserID == 0xFFFFFFFF)) {
            System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return iUserID;
        } else {
            System.out.println(ip + ":设备登录成功！");
            return iUserID;
        }
    }

    /**
     * 设备登录V40 与V30功能一致
     *
     * @param ip   设备IP
     * @param port SDK端口，默认设备的8000端口
     * @param user 设备用户名
     * @param psw  设备密码
     */
    public Tuple2<Integer, HCNetSDK.NET_DVR_DEVICEINFO_V40> Login_V40(String ip,
                                                                      short port,
                                                                      String user,
                                                                      String psw) {
        //注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        String m_sDeviceIP = ip;//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = user;//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = psw;//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = port;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
//        m_strLoginInfo.byLoginMode=1;  //ISAPI登录
        m_strLoginInfo.write();

        int iUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (iUserID < 0) {
            System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return null;
        } else {
            System.out.println(ip + ":设备登录成功！userId:" + iUserID);
//            Map<String ,Object> userInfo = new HashMap<>();
//            userInfo.put("userId", iUserID);
//            userInfo.put("charset", m_strDeviceInfo.byCharEncodeType);
            return Tuples.of(iUserID, m_strDeviceInfo);
        }
    }

    //设备在线状态监测
    public boolean getDeviceStatus(int lUserID) {
        return hCNetSDK.NET_DVR_RemoteControl(lUserID, HCNetSDK.NET_DVR_CHECK_USER_STATUS, null, 0);
    }

    //设备抓图
    public void circleGetPic(int iUserID) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        String curTime0 = sdf.format(new Date());
        Boolean result = false;
        int count = 0;
        while (!result) {
            try {
                Thread.sleep(1 * 1000); //设置暂停的时间 5 秒
                String curTime0 = sdf.format(new Date());
                count++;
                String filename = "C:\\PIC\\" + curTime0 + count + ".jpg";
                byte[] fileByte = filename.getBytes("UTF-8");
                HCNetSDK.BYTE_ARRAY byte_array = new HCNetSDK.BYTE_ARRAY(fileByte.length);
                byte_array.read();
                byte_array.byValue = fileByte;
                byte_array.write();

                HCNetSDK.NET_DVR_JPEGPARA strJpegParm = new HCNetSDK.NET_DVR_JPEGPARA();
                strJpegParm.read();
                strJpegParm.wPicSize = 2;
                strJpegParm.wPicQuality = 0;
                Pointer pStrjPegParm = strJpegParm.getPointer();
                strJpegParm.write();
                boolean b_Cap = hCNetSDK.NET_DVR_CaptureJPEGPicture(iUserID, 1, strJpegParm, fileByte);
                if (b_Cap == false) {
                    System.out.println("抓图失败,错误码为:" + hCNetSDK.NET_DVR_GetLastError());
                    return;
                }
                System.out.println(sdf.format(new Date()) + "--循环执行第" + count + "次");
                if (count == 3) {
                    result = true;
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    //端口绑定
    public void BindPort() {
        HCNetSDK.NET_DVR_LOCAL_TCP_PORT_BIND_CFG strLocalTcpBind = new HCNetSDK.NET_DVR_LOCAL_TCP_PORT_BIND_CFG();
        strLocalTcpBind.read();
        strLocalTcpBind.wLocalBindTcpMinPort = 30000;
        strLocalTcpBind.wLocalBindTcpMaxPort = 30200;
        strLocalTcpBind.write();
        Pointer pStrLocalTcoBind = strLocalTcpBind.getPointer();
        if (hCNetSDK.NET_DVR_SetSDKLocalCfg(0, pStrLocalTcoBind) == false) {
            System.out.println("绑定失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
        }
        System.out.println("绑定成功");
    }

    //获取设备的基本参数
    public void GetCfg(int iUserID) {
        HCNetSDK.NET_DVR_DEVICECFG_V40 m_strDeviceCfg = new HCNetSDK.NET_DVR_DEVICECFG_V40();
        m_strDeviceCfg.dwSize = m_strDeviceCfg.size();
        m_strDeviceCfg.write();
        Pointer pStrDeviceCfg = m_strDeviceCfg.getPointer();
        IntByReference pInt = new IntByReference(0);
        boolean b_GetCfg = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_DEVICECFG_V40,
                                                         0Xffffffff, pStrDeviceCfg, m_strDeviceCfg.dwSize, pInt);
        if (b_GetCfg == false) {
            System.out.println("获取参数失败  错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        System.out.println("获取参数成功");
        m_strDeviceCfg.read();
        System.out.println("设备名称:" + new String(m_strDeviceCfg.sDVRName).trim() + "设备序列号：" + new String(m_strDeviceCfg.sSerialNumber));
        System.out.println("模拟通道个数" + m_strDeviceCfg.byChanNum);
    }

    //获取设备时间参数
    public void GetTime(int iUserID) {
        HCNetSDK.NET_DVR_TIME m_Time = new HCNetSDK.NET_DVR_TIME();
        Pointer pTime = m_Time.getPointer();
        IntByReference pInt = new IntByReference(0);
        boolean b_GetTime = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_TIMECFG, 0xffffffff, pTime, m_Time
                .size(), pInt);
        if (b_GetTime == false) {
            System.out.println("获取时间参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        m_Time.read();
        System.out.println("年：" + m_Time.dwYear + "月:" + m_Time.dwMonth + "日:" + m_Time.dwDay + "时：" + m_Time.dwHour +
                                   "分：" + m_Time.dwMinute + "秒：" + m_Time.dwSecond);
    }

    //获取设备的图像参数
    public void GetPicCfg(int iUserID) {
        HCNetSDK.NET_DVR_PICCFG_V40 strPicCfg = new HCNetSDK.NET_DVR_PICCFG_V40();
        strPicCfg.dwSize = strPicCfg.size();
        strPicCfg.write();
        Pointer pStrPicCfg = strPicCfg.getPointer();
        NativeLong lChannel = new NativeLong(1);
        IntByReference pInt = new IntByReference(0);
        boolean b_GetPicCfg = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_PICCFG_V40, lChannel.intValue(),
                                                            pStrPicCfg, strPicCfg.size(), pInt);
        if (b_GetPicCfg == false) {
            System.out.println("获取图像参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        strPicCfg.read();
        System.out.println("通道名称：" + new String(strPicCfg.sChanName).trim() + "预览的图像是否显示OSD：" + strPicCfg.dwShowOsd);
    }

    //网络流量检测
    public void netFlowDec(int iUserID) throws InterruptedException {
        HCNetSDK.NET_DVR_FLOW_TEST_PARAM struFlowPam = new HCNetSDK.NET_DVR_FLOW_TEST_PARAM();
        struFlowPam.read();
        struFlowPam.dwSize = struFlowPam.size();
        struFlowPam.lCardIndex = 0;
        struFlowPam.dwInterval = 1;
        struFlowPam.write();
        Pointer pUser = null;
        if (flowcallback == null) {
            flowcallback = new flowTestcallback();
        }
        int FlowHandle = hCNetSDK.NET_DVR_StartNetworkFlowTest(iUserID, struFlowPam, flowcallback, pUser);
        if (FlowHandle <= -1) {
            System.out.println("开启流量检测失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("开启流量检测成功");
        }
        Thread.sleep(20000);
        hCNetSDK.NET_DVR_StopNetworkFlowTest(FlowHandle);
    }

    //录像起止时间查询
    public static void searchRecordTime(int iUserID) {
        HCNetSDK.NET_DVR_RECORD_TIME_SPAN_INQUIRY struRecInq = new HCNetSDK.NET_DVR_RECORD_TIME_SPAN_INQUIRY();
        struRecInq.read();
        struRecInq.dwSize = struRecInq.size();
        struRecInq.byType = 0;
        struRecInq.write();
        HCNetSDK.NET_DVR_RECORD_TIME_SPAN struRecSpan = new HCNetSDK.NET_DVR_RECORD_TIME_SPAN();
        //通道号说明：一般IPC/IPD通道号为1，32路以及以下路数的NVR的IP通道通道号从33开始，64路及以上路数的NVR的IP通道通道号从1开始。
        if (hCNetSDK.NET_DVR_InquiryRecordTimeSpan(iUserID, 35, struRecInq, struRecSpan) == false) {
            System.out.println("录像起止时间查询失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("录像起止时间查询成功");
            struRecSpan.read();
            System.out.println("开启时间：" + "年：" + struRecSpan.strBeginTime.dwYear + "\n");
            System.out.println("开启时间：" + "月：" + struRecSpan.strBeginTime.dwMonth + "\n");
            System.out.println("开启时间：" + "日：" + struRecSpan.strBeginTime.dwDay + "\n");
            System.out.println("开启时间：" + "时：" + struRecSpan.strBeginTime.dwHour + "\n");
            System.out.println("停止时间：" + "年：" + struRecSpan.strEndTime.dwYear + "\n");
            System.out.println("停止时间：" + "月：" + struRecSpan.strEndTime.dwMonth + "\n");
            System.out.println("停止时间：" + "日：" + struRecSpan.strEndTime.dwDay + "\n");
            System.out.println("停止时间：" + "时：" + struRecSpan.strEndTime.dwHour + "\n");
        }
    }

    //月历录像查询
    public byte[] GetRecMonth(int iUserID,
                              int year,
                              int month,
                              int dwChannel) {
        HCNetSDK.NET_DVR_MRD_SEARCH_PARAM struMrdSeaParam = new HCNetSDK.NET_DVR_MRD_SEARCH_PARAM();
        struMrdSeaParam.read();
        struMrdSeaParam.dwSize = struMrdSeaParam.size();
        struMrdSeaParam.wYear = (short) year;
        struMrdSeaParam.byMonth = (byte) month;
        //通道号说明：一般IPC/IPD通道号为1，32路以及以下路数的NVR的IP通道通道号从33开始，64路及以上路数的NVR的IP通道通道号从1开始。
        struMrdSeaParam.struStreamInfo.dwChannel = dwChannel;
        struMrdSeaParam.write();
        HCNetSDK.NET_DVR_MRD_SEARCH_RESULT struMrdSeaResu = new HCNetSDK.NET_DVR_MRD_SEARCH_RESULT();
        struMrdSeaResu.read();
        struMrdSeaResu.dwSize = struMrdSeaResu.size();
        struMrdSeaResu.write();
        IntByReference list = new IntByReference(0);
        boolean b_GetResult = hCNetSDK.NET_DVR_GetDeviceConfig(iUserID, HCNetSDK.NET_DVR_GET_MONTHLY_RECORD_DISTRIBUTION, 0, struMrdSeaParam
                                                                       .getPointer(),
                                                               struMrdSeaParam.size(), list.getPointer(), struMrdSeaResu
                                                                       .getPointer(), struMrdSeaResu.size());
        if (b_GetResult == false) {
            System.out.println("月历录像查询失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            struMrdSeaResu.read();
            for (int i = 0; i < 32; i++) {
                int day = i + 1;
                System.out.println("" + day + "号是否录像文件" + struMrdSeaResu.byRecordDistribution[i]);
            }
        }
        return struMrdSeaResu.byRecordDistribution;
    }

    //球机GIS信息获取
    public void getGisInfo(int iUserID) {
        HCNetSDK.NET_DVR_STD_CONFIG struStdCfg = new HCNetSDK.NET_DVR_STD_CONFIG();
        HCNetSDK.NET_DVR_GIS_INFO struGisInfo = new HCNetSDK.NET_DVR_GIS_INFO();
        struStdCfg.read();
        IntByReference lchannel = new IntByReference(1);
        struStdCfg.lpCondBuffer = lchannel.getPointer();
        struStdCfg.dwCondSize = 4;
        struStdCfg.lpOutBuffer = struGisInfo.getPointer();
        struStdCfg.dwOutSize = struGisInfo.size();
        struStdCfg.write();//设置前之前要write()
        boolean getSTDConfig = hCNetSDK.NET_DVR_GetSTDConfig(iUserID, HCNetSDK.NET_DVR_GET_GISINFO, struStdCfg);
        if (getSTDConfig == false) {
            System.out.println("查询GIS信息失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            struGisInfo.read();
            System.out.println("查询成功\n");
            System.out.println(struGisInfo.struPtzPos.fPanPos + "\n");
            System.out.println(struGisInfo.struPtzPos.fTiltPos + "\n");
            System.out.println(struGisInfo.struPtzPos.fZoomPos + "\n");
            System.out.println(struGisInfo.fHorizontalValue);
            System.out.println(struGisInfo.fVerticalValue);
        }

    }

    //球机PTZ参数获取设置
    public boolean SetPTZcfg(int iUserID,
                             HCNetSDK.NET_DVR_PTZPOS struPtTZPos) {
        int wPanPos = Integer.parseInt(Integer.toHexString(struPtTZPos.wPanPos).trim());
        float WPanPos = wPanPos * 0.1f;
        int wTiltPos = Integer.parseInt(Integer.toHexString(struPtTZPos.wTiltPos).trim());
        float WTiltPos = wTiltPos * 0.1f;
        int wZoomPos = Integer.parseInt(Integer.toHexString(struPtTZPos.wZoomPos).trim());
        float WZoomPos = wZoomPos * 0.1f;
        System.out.println("P参数：" + WPanPos + "\n");
        System.out.println("T参数：" + WTiltPos + "\n");
        System.out.println("Z参数：" + WZoomPos + "\n");

        struPtTZPos.wAction = 2;
        //本结构体中的wAction参数是设置时的操作类型，因此获取时该参数无效。实际显示的PTZ值是获取到的十六进制值的十分之一，
        // 如获取的水平参数P的值是0x1750，实际显示的P值为175度；获取到的垂直参数T的值是0x0789，实际显示的T值为78.9度，如果T未负值，获取的值减去360
        // 获取到的变倍参数Z的值是0x1100，实际显示的Z值为110倍。
//        String pHex="13669";
//        int pInter=Integer.parseInt(pHex);
        struPtTZPos.write();
        boolean b_SetPTZ = hCNetSDK.NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_PTZPOS, 1, struPtTZPos.getPointer(), struPtTZPos
                .size());
        if (b_SetPTZ == false) {
            System.out.println("设置PTZ坐标信息失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("设置PTZ成功");
        }
        ptzcfg.put(iUserID, struPtTZPos);
        return b_SetPTZ;

    }

    public HCNetSDK.NET_DVR_PTZPOS GetPTZcfg(int iUserID) {
        HCNetSDK.NET_DVR_PTZPOS cfg = ptzcfg.compute(iUserID, (userId, struPtTZPos) -> {
            if (struPtTZPos == null) {
                struPtTZPos = new HCNetSDK.NET_DVR_PTZPOS();
                IntByReference pUsers = new IntByReference(1);
                boolean b_GetPTZ = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_PTZPOS, 1, struPtTZPos.getPointer(), struPtTZPos
                        .size(), pUsers);
            }
            return struPtTZPos;
        });

        return cfg;
    }

    //获取(设置)前端参数(扩展)
    public static void getCameraPara(int iUserID) {
        HCNetSDK.NET_DVR_CAMERAPARAMCFG_EX struCameraParam = new HCNetSDK.NET_DVR_CAMERAPARAMCFG_EX();
        Pointer pstruCameraParam = struCameraParam.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_CCDPARAMCFG_EX, 1, pstruCameraParam, struCameraParam
                .size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            System.out.println("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struCameraParam.read();
        System.out.println("是否开启旋转：" + struCameraParam.struCorridorMode.byEnableCorridorMode);

        struCameraParam.struCorridorMode.byEnableCorridorMode = 1;
        struCameraParam.write();
        boolean b_SetCameraParam = hCNetSDK.NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_CCDPARAMCFG_EX, 1, pstruCameraParam, struCameraParam
                .size());
        if (!b_SetCameraParam) {
            System.out.println("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struCameraParam.read();
        System.out.println("设置成功");
    }

    //获取快球聚焦模式信息。
    public static void GetFocusMode(int iUserID) {
        HCNetSDK.NET_DVR_FOCUSMODE_CFG struFocusMode = new HCNetSDK.NET_DVR_FOCUSMODE_CFG();
        struFocusMode.read();
        struFocusMode.dwSize = struFocusMode.size();
        struFocusMode.write();
        Pointer pFocusMode = struFocusMode.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_FOCUSMODECFG, 1, pFocusMode, struFocusMode
                .size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            System.out.println("获取快球聚焦模式失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struFocusMode.read();
        System.out.println("聚焦模式：" + struFocusMode.byFocusMode);
        struFocusMode.byFocusMode = 1;
        struFocusMode.byFocusDefinitionDisplay = 1;
        struFocusMode.byFocusSpeedLevel = 3;
        struFocusMode.write();
        boolean b_SetCameraParam = hCNetSDK.NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_FOCUSMODECFG, 1, pFocusMode, struFocusMode
                .size());
        if (!b_SetCameraParam) {
            System.out.println("设置快球聚焦模式失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struFocusMode.read();
        System.out.println("设置成功");
    }

    //获取IP通道
    public static void getIPChannelInfo(int iUserID) {
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_IPPARACFG_V40 m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG_V40();
        m_strIpparaCfg.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, 0, lpIpParaConfig, m_strIpparaCfg
                .size(), ibrBytesReturned);
        m_strIpparaCfg.read();
        System.out.println("起始数字通道号：" + m_strIpparaCfg.dwStartDChan);

        for (int iChannum = 0; iChannum < m_strIpparaCfg.dwDChanNum; iChannum++) {
            int channum = iChannum + m_strIpparaCfg.dwStartDChan;
            m_strIpparaCfg.struStreamMode[iChannum].read();
            if (m_strIpparaCfg.struStreamMode[iChannum].byGetStreamType == 0) {
                m_strIpparaCfg.struStreamMode[iChannum].uGetStream.setType(HCNetSDK.NET_DVR_IPCHANINFO.class);
                m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.read();
                if (m_strIpparaCfg.struStreamMode[iChannum].uGetStream.struChanInfo.byEnable == 1) {
                    System.out.println("IP通道" + channum + "在线");

                } else {

                    System.out.println("IP通道" + channum + "不在线");

                }
            }
        }
    }

    //定时巡检设备
    public void regularInspection() {
        HCNetSDK.NET_DVR_CHECK_DEV_STATE struCheckStatus = new HCNetSDK.NET_DVR_CHECK_DEV_STATE();
        struCheckStatus.read();
        struCheckStatus.dwTimeout = 1000; //定时检测设备工作状态，单位：ms，0表示使用默认值(30000)，最小值为1000
        if (workStateCb == null) {
            workStateCb = new dev_work_state_cb();
        }
        struCheckStatus.fnStateCB = workStateCb;
        struCheckStatus.write();
        boolean b_state = hCNetSDK.NET_DVR_StartGetDevState(struCheckStatus);
        if (!b_state) {
            System.out.println("定时巡检设备状态失败：" + hCNetSDK.NET_DVR_GetLastError());
        }
    }

    //获取高精度PTZ绝对位置配置
    public static void getPTZAbsoluteEx(int iUserID) {
        HCNetSDK.NET_DVR_STD_CONFIG struSTDcfg = new HCNetSDK.NET_DVR_STD_CONFIG();
        HCNetSDK.NET_DVR_PTZABSOLUTEEX_CFG struPTZ = new HCNetSDK.NET_DVR_PTZABSOLUTEEX_CFG();
        struSTDcfg.read();
        IntByReference channel = new IntByReference(1);
        struSTDcfg.lpCondBuffer = channel.getPointer();
        struSTDcfg.dwCondSize = 4;
        struSTDcfg.lpOutBuffer = struPTZ.getPointer();
        struSTDcfg.dwOutSize = struPTZ.size();
        struSTDcfg.lpInBuffer = Pointer.NULL;
        struSTDcfg.dwInSize = 0;
        struSTDcfg.write();
        boolean bGetPTZ = hCNetSDK.NET_DVR_GetSTDConfig(iUserID, HCNetSDK.NET_DVR_GET_PTZABSOLUTEEX, struSTDcfg);
        if (bGetPTZ == false) {
            System.out.println("获取PTZ参数错误,错误码：" + hCNetSDK.NET_DVR_GetLastError());
            return;
        }
        struPTZ.read();
        System.out.println("焦距范围：" + struPTZ.dwFocalLen);
        System.out.println("聚焦参数：" + struPTZ.struPTZCtrl.dwFocus);
        return;
    }

    //获取GB28181参数
    public static void GetGB28181Info(int iUserID) {

        HCNetSDK.NET_DVR_STREAM_INFO streamInfo = new HCNetSDK.NET_DVR_STREAM_INFO();
        streamInfo.read();
        streamInfo.dwSize = streamInfo.size(); //设置结构体大小
        streamInfo.dwChannel = 1; //设置通道
        streamInfo.write();
        Pointer lpInBuffer = streamInfo.getPointer();
        HCNetSDK.NET_DVR_GBT28181_CHANINFO_CFG gbt28181ChaninfoCfg = new HCNetSDK.NET_DVR_GBT28181_CHANINFO_CFG();
        gbt28181ChaninfoCfg.read();
        gbt28181ChaninfoCfg.dwSize = gbt28181ChaninfoCfg.size();
        gbt28181ChaninfoCfg.write();
        Pointer lpOutBuffer = gbt28181ChaninfoCfg.getPointer();
        IntByReference lpBytesReturned = new IntByReference(0);
        //3251对应它的宏定义
        boolean bRet = hCNetSDK.NET_DVR_GetDeviceConfig(iUserID, 3251, 1, lpInBuffer,
                                                        streamInfo.size(), lpBytesReturned.getPointer(), lpOutBuffer, gbt28181ChaninfoCfg
                                                                .size());
        gbt28181ChaninfoCfg.read();

        if (bRet == false) {
            System.out.println("获取失败,错误码：" + hCNetSDK.NET_DVR_GetLastError());
            return;
        }
    }

    //获取码流加密信息
    public HCNetSDK.NET_DVR_AES_KEY_INFO getAESInfo(int iUserID) {
        HCNetSDK.NET_DVR_AES_KEY_INFO net_dvr_aes_key_info = new HCNetSDK.NET_DVR_AES_KEY_INFO();
        net_dvr_aes_key_info.read();
        Pointer pnet_dvr_aes_key_info = net_dvr_aes_key_info.getPointer();
        IntByReference pInt = new IntByReference(0);
        boolean b_GetCfg = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_AES_KEY,
                                                         0Xffffffff, pnet_dvr_aes_key_info, net_dvr_aes_key_info.size(), pInt);
        if (b_GetCfg == false) {
            System.out.println("获取码流加密失败  错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        System.out.println("获取码流加密信息成功");
        return net_dvr_aes_key_info;

    }


    //设置球机预置点
    public static void getCRUISEPOINT(int iUserID) {
        HCNetSDK.NET_DVR_CRUISEPOINT_COND struCruisepointCond = new HCNetSDK.NET_DVR_CRUISEPOINT_COND();
        struCruisepointCond.read();
        struCruisepointCond.dwSize = struCruisepointCond.size();
        struCruisepointCond.dwChan = 1;
        struCruisepointCond.wRouteNo = 1;
        struCruisepointCond.write();

        HCNetSDK.NET_DVR_CRUISEPOINT_V50 struCruisepointV40 = new HCNetSDK.NET_DVR_CRUISEPOINT_V50();
        struCruisepointV40.read();
        struCruisepointV40.dwSize = struCruisepointV40.size();
        struCruisepointV40.write();

        // 错误信息列表
        IntByReference pInt = new IntByReference(0);
        Pointer lpStatusList = pInt.getPointer();

        boolean flag = hCNetSDK.NET_DVR_GetDeviceConfig(iUserID, 6714, 1,
                                                        struCruisepointCond.getPointer(), struCruisepointCond.size(), lpStatusList, struCruisepointV40
                                                                .getPointer(), struCruisepointV40.size());
        if (flag == false) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return;
        }
        struCruisepointV40.read();


    }

    //抓图保存到缓冲区
    public static void getPic_new(int iUserID) {
        HCNetSDK.NET_DVR_JPEGPARA jpegpara = new HCNetSDK.NET_DVR_JPEGPARA();
        jpegpara.read();
        jpegpara.wPicSize = 255;
        jpegpara.wPicQuality = 0;
        jpegpara.write();
        HCNetSDK.BYTE_ARRAY byte_array = new HCNetSDK.BYTE_ARRAY(10 * 1024 * 1024);
        IntByReference ret = new IntByReference(0);
        boolean b = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(iUserID, 1, jpegpara, byte_array.getPointer(), byte_array.size(), ret);
        if (b == false) {
            System.out.println("抓图失败：" + hCNetSDK.NET_DVR_GetLastError());
            return;
        }
        byte_array.read();
        System.out.println("抓图成功");
        return;
    }

    /**
     * 获取报警主机RS485参数
     *
     * @param iUserID
     */
    public HCNetSDK.NET_DVR_ALARM_RS485CFG getRS485Cfg(int iUserID) {
        HCNetSDK.NET_DVR_ALARM_RS485CFG cfg = rs485cfg.compute(iUserID, (userId, rs485CFG) -> {
            if (rs485CFG == null) {
                rs485CFG = new HCNetSDK.NET_DVR_ALARM_RS485CFG();
                rs485CFG.dwSize = rs485CFG.size();
                Pointer pointer = rs485CFG.getPointer();
                IntByReference pInt1 = new IntByReference(0);
                rs485CFG.write();
                boolean bGetRs485 = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_ALARM_RS485CFG, 3, pointer, rs485CFG.dwSize, pInt1);
                if (!bGetRs485) {
                    System.out.println("获取报警主机RS485参数失败！错误号：" + hCNetSDK.NET_DVR_GetLastError());
                }
                rs485CFG.read();
            }
            return rs485CFG;
        });

        return cfg;
    }

    public boolean setRS485Cfg(int iUserID,
                               HCNetSDK.NET_DVR_ALARM_RS485CFG cfg) {
        cfg.dwSize = cfg.size();
        Pointer pointer = cfg.getPointer();
        cfg.write();
        boolean success = hCNetSDK.NET_DVR_SetDVRConfig(iUserID, HCNetSDK.NET_DVR_SET_ALARM_RS485CFG, 3, pointer, cfg.dwSize);
        rs485cfg.put(iUserID, cfg);
        return true;
    }

    public static void getRS485SlotInfo(int iUserID) {
        HCNetSDK.NET_DVR_ALARMHOST_RS485_SLOT_CFG strRs485SlotCFG = new HCNetSDK.NET_DVR_ALARMHOST_RS485_SLOT_CFG();
        strRs485SlotCFG.dwSize = strRs485SlotCFG.size();
        Pointer pRs485SlotCFG = strRs485SlotCFG.getPointer();
        IntByReference pInt1 = new IntByReference(0);
        strRs485SlotCFG.write();
        String Schannel = "0000000100000001";  //高2字节表示485通道号，低2字节表示槽位号，都从1开始
        int channel = Integer.parseInt(Schannel, 2);
        boolean bRs485Slot = hCNetSDK.NET_DVR_GetDVRConfig(iUserID, HCNetSDK.NET_DVR_GET_ALARMHOST_RS485_SLOT_CFG, channel, pRs485SlotCFG, strRs485SlotCFG.dwSize, pInt1);
        if (!bRs485Slot) {
            System.out.println("获取报警主机RS485槽位参数失败！错误号：" + hCNetSDK.NET_DVR_GetLastError());
            return;
        }
        strRs485SlotCFG.read();
        return;

    }

    public LPNET_DVR_LINK_ADDR realPlay(int userId , MediaSdkChannelConfig config) {
        int lLinkHandle = preview(userId, config);
        LPNET_DVR_LINK_ADDR lpLinkAddr = new LPNET_DVR_LINK_ADDR();
        hCNetSDK.NET_DVR_GetLinkAddr(lLinkHandle, 1, lpLinkAddr);
        return lpLinkAddr;
    }

    public LPNET_DVR_LINK_ADDR playback(int userId , MediaSdkChannelConfig config) {
        int lLinkHandle = preview(userId, config);
        LPNET_DVR_LINK_ADDR lpLinkAddr = new LPNET_DVR_LINK_ADDR();
        hCNetSDK.NET_DVR_GetLinkAddr(lLinkHandle, 2, lpLinkAddr);
        return lpLinkAddr;
    }

    private LPNET_DVR_LINK_ADDR getStreamAddr(int userId , MediaSdkChannelConfig config, int enumLinkKind) {
        int lLinkHandle = preview(userId, config);
        LPNET_DVR_LINK_ADDR lpLinkAddr = new LPNET_DVR_LINK_ADDR();
        hCNetSDK.NET_DVR_GetLinkAddr(lLinkHandle, enumLinkKind, lpLinkAddr);
        return lpLinkAddr;
    }

    public LinkedList<Integer> createDeviceTreeV40(int userId, HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceinfo) {
        LinkedList<Integer> channelNo = new LinkedList<>();
        HCNetSDK.NET_DVR_DEVICEINFO_V30 strDeviceInfo = deviceinfo.struDeviceV30;
        IntByReference ibrBytesReturned = new IntByReference(0);// get ip param
        HCNetSDK.NET_DVR_IPPARACFG strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
        Pointer lpIpParaConfig = strIpparaCfg.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(userId, HCNetSDK.NET_DVR_GET_IPPARACFG, 1, lpIpParaConfig,
                                                     strIpparaCfg.size(), ibrBytesReturned);
        log.info("获取设备信息：{}", bRet);
        strIpparaCfg.read();
        if (!bRet) {
            // device not support,has no ip camera
            for (int iChannum = 0; iChannum < strDeviceInfo.byChanNum; iChannum++) {
                log.info("通道号：{}", iChannum + strDeviceInfo.byStartChan + 32);
                channelNo.add(iChannum + strDeviceInfo.byStartChan + 32);
            }
        } else {
            // ip camera
            for (int iChannum = 0; iChannum < strDeviceInfo.byChanNum; iChannum++) {
                {
                    log.info("通道号：{}", iChannum + strDeviceInfo.byStartChan + 32);
                    channelNo.add(iChannum + strDeviceInfo.byStartChan + 32);
                }
            }
            for (int iChannum = 0; iChannum < strDeviceInfo.byIPChanNum; iChannum++) {
                log.info("通道号：{}", iChannum + strDeviceInfo.byStartChan + 32);
                channelNo.add(iChannum + strDeviceInfo.byStartChan + 32);
            }
        }
        return channelNo;
    }

    // 查询rtsp端口
    public int queryRtspPort(int lUserID) {
        HCNetSDK.NET_DVR_RTSPCFG rtspcfg = new HCNetSDK.NET_DVR_RTSPCFG();
        rtspcfg.dwSize = rtspcfg.size();

        hCNetSDK.NET_DVR_GetRtspConfig(lUserID, 0, rtspcfg, rtspcfg.size());
        return rtspcfg.wPort;
    }

    // 查询通道
    public HCNetSDK.NET_DVR_ACCESS_DEVICE_CHANNEL_INFO queryChannel(int iUserID,
                                                                    int dwChannel,
                                                                    String userName,
                                                                    String password,
                                                                    String ip,
                                                                    short port) {
        HCNetSDK.NET_DVR_ACCESS_DEVICE_INFO accessDevice = new HCNetSDK.NET_DVR_ACCESS_DEVICE_INFO();
        accessDevice.read();
        accessDevice.dwSize = accessDevice.size();
        System.arraycopy(userName.getBytes(), 0, accessDevice.sUserName, 0, userName.length());
        System.arraycopy(password.getBytes(), 0, accessDevice.szPassword, 0, password.length());
        HCNetSDK.NET_DVR_IPADDR ipaddr = new HCNetSDK.NET_DVR_IPADDR();
        System.arraycopy(ip.getBytes(), 0, ipaddr.sIpV4, 0, ip.length());
        accessDevice.wPort = port;
        //通道号说明：一般IPC/IPD通道号为1，32路以及以下路数的NVR的IP通道通道号从33开始，64路及以上路数的NVR的IP通道通道号从1开始。
        accessDevice.write();

        HCNetSDK.NET_DVR_ACCESS_DEVICE_CHANNEL_INFO info = new HCNetSDK.NET_DVR_ACCESS_DEVICE_CHANNEL_INFO();
        info.read();
        info.dwSize = info.size();
        info.write();
        IntByReference list = new IntByReference(0);
        boolean b_GetResult = hCNetSDK.NET_DVR_GetDeviceConfig(iUserID,
                                                               HCNetSDK.NET_DVR_GET_ACCESS_DEVICE_CHANNEL_INFO,
                                                               dwChannel,
                                                               accessDevice.getPointer(),
                                                               accessDevice.size(),
                                                               list.getPointer(),
                                                               info.getPointer(),
                                                               info.size());
        if (b_GetResult == false) {
            System.out.println("设备通道查询失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            info.read();
        }
        return info;
    }

    // 查询预置位
    public List<HCNetSDK.NET_DVR_PRESET_NAME> queryPreset(int lUserID) {

        HCNetSDK.NET_DVR_PRESET_NAME presetName = new HCNetSDK.NET_DVR_PRESET_NAME();
        presetName.dwSize = presetName.size();
        presetName.write();
        Pointer pointer = presetName.getPointer();

        IntByReference pInt = new IntByReference(0);
        boolean result = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_PRESET_NAME, 0xFFFFFFFF,
                                                       pointer, presetName.size(), pInt);
        System.out.println(pInt.getValue());
//        HCNetSDK.NET_DVR_PRESET_NAME[] presetNames = (HCNetSDK.NET_DVR_PRESET_NAME[])presetName.toArray(10);
//        Pointer[] pointers = new Pointer[300];
//        for (int i = 0; i < presetNames.length; i++) {
//            pointers[i] = presetNames[i].getPointer();
//        }
//        pointer.read(0, pointers, 0, 300);
        if (!result) {
            log.error("获取预置位失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }

        presetName.read();

        // TODO: 2023/10/20 返回列表。如何从一个pointer读取结构体数组？
        return Arrays.asList(presetName);
    }

    // 预置位控制
    public boolean presetControl(int userId, MediaSdkDeviceControl control) {
        int dwPTZPresetCmd = getPTZPresetCmd(control.getPresetOperation());
        int channel = Integer.parseInt(control.getChannelId());
        // 不用启动预览
        return hCNetSDK.NET_DVR_PTZPreset_Other(
                userId, channel, dwPTZPresetCmd, control.getPresetIndex()
        );
    }

    private int getPTZPresetCmd(MediaSdkDeviceControl.PresetOperation operation) {
        switch (operation) {
            case DEL:
                return HCNetSDK.CLE_PRESET;
            case SET:
                return HCNetSDK.SET_PRESET;
            case CALL:
                return HCNetSDK.GOTO_PRESET;
            default:
                throw new BusinessException("不支持的预置位操作：{}", operation.name());
        }
    }

    // 云台控制
    public PtzCommand ptzControl(int userId, MediaSdkDeviceControl control, int lastCmd) {
        int channel = Integer.parseInt(control.getChannelId());
        PtzCommand ptzCommand = getPTZCmd(control.getPtzCommand(), lastCmd);

        ptzCommand.success = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(
                userId, channel, ptzCommand.command, ptzCommand.stop, ptzCommand.speed
        );
        return ptzCommand;
    }

    @Getter
    @Setter
    public static class PtzCommand {

        int command;
        // 0：启动，1：停止
        int stop;
        // 范围[1 - 7]
        int speed;

        boolean success;
    }

    /**
     * 获取控制指令
     * @param ptzCommand 指令集合
     * @param lastCmd 缓存的上一次指令
     * @return 指令
     */
    private PtzCommand getPTZCmd(Map<String, Integer> ptzCommand, int lastCmd) {
        PtzCommand cmd = new PtzCommand();
        Set<MediaSdkDeviceControl.PtzCommand> directions = ptzCommand
                .keySet()
                .stream()
                .map(String::toUpperCase)
                .map(MediaSdkDeviceControl.PtzCommand::valueOf)
                .collect(Collectors.toSet());


        if (directions.contains(MediaSdkDeviceControl.PtzCommand.STOP)) {
            cmd.stop = 1;
            cmd.command = lastCmd;
        } else {
            cmd.command = doGetPtzCmd(directions);
        }
        cmd.speed = ptzCommand.values().stream().findFirst().map(value -> value * 7 / 180).orElse(1);
        return cmd;
    }

    private int doGetPtzCmd(Set<MediaSdkDeviceControl.PtzCommand> directions) {
        // 只实现了最多2个方向的控制
        if (directions.size() <= 2) {
            if (directions.contains(MediaSdkDeviceControl.PtzCommand.UP)) {
                if (directions.contains(MediaSdkDeviceControl.PtzCommand.LEFT)) {
                    return UP_LEFT;
                }
                if (directions.contains(MediaSdkDeviceControl.PtzCommand.RIGHT)) {
                    return UP_RIGHT;
                }
                return TILT_UP;
            }
            if (directions.contains(MediaSdkDeviceControl.PtzCommand.DOWN)) {
                if (directions.contains(MediaSdkDeviceControl.PtzCommand.LEFT)) {
                    return DOWN_LEFT;
                }
                if (directions.contains(MediaSdkDeviceControl.PtzCommand.RIGHT)) {
                    return DOWN_RIGHT;
                }
                return TILT_DOWN;
            }
            if (directions.contains(MediaSdkDeviceControl.PtzCommand.LEFT)) {
                return PAN_LEFT;
            }
            if (directions.contains(MediaSdkDeviceControl.PtzCommand.RIGHT)) {
                return PAN_RIGHT;
            }
            if (directions.contains(MediaSdkDeviceControl.PtzCommand.ZOOM_IN)) {
                return ZOOM_IN;
            }
            if (directions.contains(MediaSdkDeviceControl.PtzCommand.ZOOM_OUT)) {
                return ZOOM_OUT;
            }
        }

        throw new BusinessException("暂不支持此指令：" + directions);
    }

    // 预览
    private int preview(int userId, MediaSdkChannelConfig config) {
        // 预览设置
        HCNetSDK.NET_DVR_PREVIEWINFO streamInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        streamInfo.hPlayWnd = null; //null表示不播放视频
        streamInfo.lChannel = Integer.parseInt(config.getChannel()); //通道号
        streamInfo.dwStreamType = (int) config.getExtraConfig("dwStreamType", 0); //码流类型
        streamInfo.dwLinkMode = (int) config.getExtraConfig("dwLinkMode", 3); //流传输模式
        streamInfo.bBlocked = 1; //阻塞取流
        // 开始预览
        return hCNetSDK.NET_DVR_RealPlay_V40(userId, streamInfo, null, null);
    }

    // 开始录像
    public boolean startRecord(int userId, int channel) {
        // 录像类型：0- 手动，1- 报警，2- 回传，3- 信号，4- 移动，5- 遮挡
        return hCNetSDK.NET_DVR_StartDVRRecord(userId, channel, 0);
    }

    // 停止录像
    public boolean stopRecord(int userId, int channel) {
        return hCNetSDK.NET_DVR_StopDVRRecord(userId, channel);
    }


    // 查询录像文件
    public List<NET_DVR_FINDDATA_V30> queryRecord(int userId, int channel, LocalDateTime start, LocalDateTime stop) {

        NET_DVR_FILECOND param = new NET_DVR_FILECOND();
        param.lChannel = channel;
        param.struStartTime = NET_DVR_TIME.of(start);
        param.struStopTime = NET_DVR_TIME.of(stop);
        param.dwFileType = 0XFF; //要查找的文件类型：0xFF－全部；0－定时录像；1—移动侦测；2－接近报警；3－出钞报警；4－进钞报警；5—命令触发；6－手动录像；7－震动报警；8-环境报警；9-智能报警
        param.dwIsLocked = 0XFF;

        int findHandle = hCNetSDK.NET_DVR_FindFile_V30(userId, param);
        if (findHandle == -1) {
            throw new BusinessException("查询录像失败。code：" + hCNetSDK.NET_DVR_GetLastError());
        }

        List<NET_DVR_FINDDATA_V30> list = new ArrayList<>();
        int status;
        do {
            NET_DVR_FINDDATA_V30 data = new NET_DVR_FINDDATA_V30();
            status = hCNetSDK.NET_DVR_FindNextFile_V30(findHandle, data);
            if (status == NET_DVR_FILE_SUCCESS) {
                data.read();
                list.add(data);
            }
        } while (status == NET_DVR_FILE_SUCCESS || status == NET_DVR_ISFINDING);

        hCNetSDK.NET_DVR_FindClose_V30(findHandle);

        return list;
    }

    public boolean initSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                if (!CreateSDKInstance()) {
                    System.out.println("Load SDK fail");
                    return false;
                }
            }
        }
        //SDK初始化，一个程序进程只需要调用一次
        hCNetSDK.NET_DVR_Init();
        if (fExceptionCallBack == null) {
            fExceptionCallBack = new FExceptionCallBack_Imp();
        }
        Pointer pUser = null;
        if (!hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, fExceptionCallBack, pUser)) {
            return false;
        }
        System.out.println("设置异常消息回调成功");

        //启用SDK写日志
        hCNetSDK.NET_DVR_SetLogToFile(3, "..\\sdkLog", false);

        return true;
    }

    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                LibUtils.copyLibFile();
                String strDllPath = "";
                try {
                    if (isWindows()) {
                        //win系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "\\data\\plugins\\lib\\HCNetSDK.dll";
                    } else if (isLinux()) {
                        //Linux系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "/lib/libhcnetsdk.so";
                    }

                    hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Getter
    @Setter
    public static class DeviceInfo {

        private int charset;

    }
}


