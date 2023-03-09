package org.jetlinks.plugin.example.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.plugin.example.sdk.hc.HCNetSDK;

import java.nio.charset.StandardCharsets;

/**
 * 输入描述.
 *
 * @author zhangji 2023/3/7
 */
public class MockHCNetSDK implements HCNetSDK {

    @Override
    public boolean NET_DVR_Init() {
        return true;
    }

    @Override
    public boolean NET_DVR_Cleanup() {
        return true;
    }

    @Override
    public boolean NET_DVR_SetSDKInitCfg(int enumType,
                                         Pointer lpInBuff) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetSDKLocalCfg(int enumType,
                                          Pointer lpInBuff) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetSDKLocalCfg(int enumType,
                                          Pointer lpOutBuff) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessage(int nMessage,
                                         int hWnd) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetExceptionCallBack_V30(int nMessage,
                                                    int hWnd,
                                                    FExceptionCallBack fExceptionCallBack,
                                                    Pointer pUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessCallBack(FMessCallBack fMessCallBack) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessCallBack_EX(FMessCallBack_EX fMessCallBack_EX) {
        return true;
    }

    @Override
    public int NET_DVR_FindNextFile_Card(int lFindHandle,
                                         NET_DVR_FINDDATA_CARD lpFindData) {
        return 0;
    }

    @Override
    public int NET_DVR_FindFile_Card(int lUserID,
                                     int lChannel,
                                     int dwFileType,
                                     NET_DVR_TIME lpStartTime,
                                     NET_DVR_TIME lpStopTime) {
        return 0;
    }

    @Override
    public boolean NET_DVR_LockFileByName(int lUserID,
                                          String sLockFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_UnlockFileByName(int lUserID,
                                            String sUnlockFileName) {
        return true;
    }

    @Override
    public int NET_DVR_PlayBackByName(int lUserID,
                                      String sPlayBackFileName,
                                      W32API.HWND hWnd) {
        return 0;
    }

    @Override
    public int NET_DVR_PlayBackByTime(int lUserID,
                                      int lChannel,
                                      NET_DVR_TIME lpStartTime,
                                      NET_DVR_TIME lpStopTime,
                                      W32API.HWND hWnd) {
        return 0;
    }

    @Override
    public int NET_DVR_PlayBackByTime_V40(int lUserID,
                                          NET_DVR_VOD_PARA pVodPara) {
        return 0;
    }

    @Override
    public boolean NET_DVR_PlayBackControl(int lPlayHandle,
                                           int dwControlCode,
                                           int dwInValue,
                                           IntByReference LPOutValue) {
        return true;
    }

    @Override
    public boolean NET_DVR_PlayBackControl_V40(int lPlayHandle,
                                               int dwControlCode,
                                               Pointer lpInBuffer,
                                               int dwInLen,
                                               Pointer lpOutBuffer,
                                               IntByReference lpOutLen) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopPlayBack(int lPlayHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetPlayDataCallBack(int lPlayHandle,
                                               FPlayDataCallBack fPlayDataCallBack,
                                               int dwUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetPlayBackESCallBack(int lPlayHandle,
                                                 FPlayESCallBack fPlayESCallBack,
                                                 Pointer pUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_PlayBackSaveData(int lPlayHandle,
                                            String sFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopPlayBackSave(int lPlayHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetPlayBackOsdTime(int lPlayHandle,
                                              NET_DVR_TIME lpOsdTime) {
        return true;
    }

    @Override
    public boolean NET_DVR_PlayBackCaptureFile(int lPlayHandle,
                                               String sFileName) {
        return true;
    }

    @Override
    public int NET_DVR_GetFileByName(int lUserID,
                                     String sDVRFileName,
                                     byte[] sSavedFileName) {
        return 0;
    }

    @Override
    public int NET_DVR_GetFileByTime(int lUserID,
                                     int lChannel,
                                     NET_DVR_TIME lpStartTime,
                                     NET_DVR_TIME lpStopTime,
                                     String sSavedFileName) {
        return 0;
    }

    @Override
    public int NET_DVR_GetFileByTime_V40(int lUserID,
                                         String sSavedFileName,
                                         NET_DVR_PLAYCOND pDownloadCond) {
        return 0;
    }

    @Override
    public boolean NET_DVR_StopGetFile(int lFileHandle) {
        return true;
    }

    @Override
    public int NET_DVR_GetDownloadPos(int lFileHandle) {
        return 0;
    }

    @Override
    public int NET_DVR_GetPlayBackPos(int lPlayHandle) {
        return 0;
    }

    @Override
    public int NET_DVR_FindPicture(int lUserID,
                                   NET_DVR_FIND_PICTURE_PARAM pFindParam) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextPicture_V50(int lFindHandle,
                                           NET_DVR_FIND_PICTURE_V50 lpFindData) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextPicture(int lFindHandle,
                                       NET_DVR_FIND_PICTURE lpFindData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_CloseFindPicture(int lFindHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetPicture_V50(int lUserID,
                                          NET_DVR_PIC_PARAM lpPicParam) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessCallBack_NEW(FMessCallBack_NEW fMessCallBack_NEW) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessageCallBack(FMessageCallBack fMessageCallBack,
                                                 int dwUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessageCallBack_V30(FMSGCallBack fMessageCallBack,
                                                     Pointer pUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessageCallBack_V31(FMSGCallBack_V31 fMessageCallBack,
                                                     Pointer pUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRMessageCallBack_V50(int iIndex,
                                                     FMSGCallBack_V31 fMessageCallBack,
                                                     Pointer pUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetConnectTime(int dwWaitTime,
                                          int dwTryTimes) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetReconnect(int dwInterval,
                                        boolean bEnableRecon) {
        return true;
    }

    @Override
    public int NET_DVR_GetSDKVersion() {
        return 0;
    }

    @Override
    public int NET_DVR_GetSDKBuildVersion() {
        return 0;
    }

    @Override
    public int NET_DVR_IsSupport() {
        return 0;
    }

    @Override
    public boolean NET_DVR_StartListen(String sLocalIP,
                                       short wLocalPort) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopListen() {
        return true;
    }

    @Override
    public int NET_DVR_StartListen_V30(String sLocalIP,
                                       short wLocalPort,
                                       FMSGCallBack_V31 DataCallBack,
                                       Pointer pUserData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_StopListen_V30(int lListenHandle) {
        return true;
    }

    @Override
    public int NET_DVR_Login(String sDVRIP,
                             short wDVRPort,
                             String sUserName,
                             String sPassword,
                             NET_DVR_DEVICEINFO lpDeviceInfo) {
        return 0;
    }

    @Override
    public int NET_DVR_Login_V30(String sDVRIP,
                                 short wDVRPort,
                                 String sUserName,
                                 String sPassword,
                                 NET_DVR_DEVICEINFO_V30 lpDeviceInfo) {
        return 0;
    }

    @Override
    public int NET_DVR_Login_V40(NET_DVR_USER_LOGIN_INFO pLoginInfo,
                                 NET_DVR_DEVICEINFO_V40 lpDeviceInfo) {
        return IDGenerator.SNOW_FLAKE.generate().intValue();
    }

    @Override
    public boolean NET_DVR_Logout(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_Logout_V30(int lUserID) {
        return true;
    }

    @Override
    public int NET_DVR_GetLastError() {
        return 0;
    }

    @Override
    public String NET_DVR_GetErrorMsg(IntByReference pErrorNo) {
        return null;
    }

    @Override
    public boolean NET_DVR_SetShowMode(int dwShowType,
                                       int colorKey) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDVRIPByResolveSvr(String sServerIP,
                                                short wServerPort,
                                                String sDVRName,
                                                short wDVRNameLen,
                                                String sDVRSerialNumber,
                                                short wDVRSerialLen,
                                                String sGetIP) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDVRIPByResolveSvr_EX(String sServerIP,
                                                   short wServerPort,
                                                   String sDVRName,
                                                   short wDVRNameLen,
                                                   String sDVRSerialNumber,
                                                   short wDVRSerialLen,
                                                   String sGetIP,
                                                   IntByReference dwPort) {
        return true;
    }

    @Override
    public int NET_DVR_RealPlay(int lUserID,
                                NET_DVR_CLIENTINFO lpClientInfo) {
        return 0;
    }

    @Override
    public int NET_DVR_RealPlay_V30(int lUserID,
                                    NET_DVR_CLIENTINFO lpClientInfo,
                                    FRealDataCallBack_V30 fRealDataCallBack_V30,
                                    Pointer pUser,
                                    boolean bBlocked) {
        return 0;
    }

    @Override
    public int NET_DVR_RealPlay_V40(int lUserID,
                                    NET_DVR_PREVIEWINFO lpPreviewInfo,
                                    FRealDataCallBack_V30 fRealDataCallBack_V30,
                                    Pointer pUser) {
        return 0;
    }

    @Override
    public boolean NET_DVR_StopRealPlay(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_RigisterDrawFun(int lRealHandle,
                                           FDrawFun fDrawFun,
                                           int dwUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetPlayerBufNumber(int lRealHandle,
                                              int dwBufNum) {
        return true;
    }

    @Override
    public boolean NET_DVR_ThrowBFrame(int lRealHandle,
                                       int dwNum) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetAudioMode(int dwMode) {
        return true;
    }

    @Override
    public boolean NET_DVR_OpenSound(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_CloseSound() {
        return true;
    }

    @Override
    public boolean NET_DVR_OpenSoundShare(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_CloseSoundShare(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_Volume(int lRealHandle,
                                  short wVolume) {
        return true;
    }

    @Override
    public boolean NET_DVR_SaveRealData(int lRealHandle,
                                        String sFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopSaveRealData(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetRealDataCallBack(int lRealHandle,
                                               FRowDataCallBack fRealDataCallBack,
                                               int dwUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetStandardDataCallBack(int lRealHandle,
                                                   FStdDataCallBack fStdDataCallBack,
                                                   int dwUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_CapturePicture(int lRealHandle,
                                          String sPicFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_MakeKeyFrame(int lUserID,
                                        int lChannel) {
        return true;
    }

    @Override
    public boolean NET_DVR_MakeKeyFrameSub(int lUserID,
                                           int lChannel) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZControl(int lRealHandle,
                                      int dwPTZCommand,
                                      int dwStop) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZControl_Other(int lUserID,
                                            int lChannel,
                                            int dwPTZCommand,
                                            int dwStop) {
        return true;
    }

    @Override
    public boolean NET_DVR_TransPTZ(int lRealHandle,
                                    String pPTZCodeBuf,
                                    int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_TransPTZ_Other(int lUserID,
                                          int lChannel,
                                          String pPTZCodeBuf,
                                          int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZPreset(int lRealHandle,
                                     int dwPTZPresetCmd,
                                     int dwPresetIndex) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZPreset_Other(int lUserID,
                                           int lChannel,
                                           int dwPTZPresetCmd,
                                           int dwPresetIndex) {
        return true;
    }

    @Override
    public boolean NET_DVR_TransPTZ_EX(int lRealHandle,
                                       String pPTZCodeBuf,
                                       int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZControl_EX(int lRealHandle,
                                         int dwPTZCommand,
                                         int dwStop) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZPreset_EX(int lRealHandle,
                                        int dwPTZPresetCmd,
                                        int dwPresetIndex) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZCruise(int lRealHandle,
                                     int dwPTZCruiseCmd,
                                     byte byCruiseRoute,
                                     byte byCruisePoint,
                                     short wInput) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZCruise_Other(int lUserID,
                                           int lChannel,
                                           int dwPTZCruiseCmd,
                                           byte byCruiseRoute,
                                           byte byCruisePoint,
                                           short wInput) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZCruise_EX(int lRealHandle,
                                        int dwPTZCruiseCmd,
                                        byte byCruiseRoute,
                                        byte byCruisePoint,
                                        short wInput) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZTrack(int lRealHandle,
                                    int dwPTZTrackCmd) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZTrack_Other(int lUserID,
                                          int lChannel,
                                          int dwPTZTrackCmd) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZTrack_EX(int lRealHandle,
                                       int dwPTZTrackCmd) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZControlWithSpeed(int lRealHandle,
                                               int dwPTZCommand,
                                               int dwStop,
                                               int dwSpeed) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZControlWithSpeed_Other(int lUserID,
                                                     int lChannel,
                                                     int dwPTZCommand,
                                                     int dwStop,
                                                     int dwSpeed) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZControlWithSpeed_EX(int lRealHandle,
                                                  int dwPTZCommand,
                                                  int dwStop,
                                                  int dwSpeed) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetPTZCruise(int lUserID,
                                        int lChannel,
                                        int lCruiseRoute,
                                        NET_DVR_CRUISE_RET lpCruiseRet) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZMltTrack(int lRealHandle,
                                       int dwPTZTrackCmd,
                                       int dwTrackIndex) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZMltTrack_Other(int lUserID,
                                             int lChannel,
                                             int dwPTZTrackCmd,
                                             int dwTrackIndex) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZMltTrack_EX(int lRealHandle,
                                          int dwPTZTrackCmd,
                                          int dwTrackIndex) {
        return true;
    }

    @Override
    public int NET_DVR_FindFile(int lUserID,
                                int lChannel,
                                int dwFileType,
                                NET_DVR_TIME lpStartTime,
                                NET_DVR_TIME lpStopTime) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextFile(int lFindHandle,
                                    NET_DVR_FIND_DATA lpFindData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_FindClose(int lFindHandle) {
        return true;
    }

    @Override
    public int NET_DVR_FindNextFile_V30(int lFindHandle,
                                        NET_DVR_FINDDATA_V30 lpFindData) {
        return 0;
    }

    @Override
    public int NET_DVR_FindFile_V30(int lUserID,
                                    NET_DVR_FILECOND pFindCond) {
        return 0;
    }

    @Override
    public int NET_DVR_FindFile_V40(int lUserID,
                                    NET_DVR_FILECOND_V40 pFindCond) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextFile_V40(int lFindHandle,
                                        NET_DVR_FINDDATA_V40 lpFindData) {
        return 0;
    }

    @Override
    public int NET_DVR_FindFile_V50(int lUserID,
                                    NET_DVR_FILECOND_V50 pFindCond) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextFile_V50(int lFindHandle,
                                        NET_DVR_FINDDATA_V50 lpFindData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_FindClose_V30(int lFindHandle) {
        return true;
    }

    @Override
    public int NET_DVR_FindFileByEvent(int lUserID,
                                       NET_DVR_SEARCH_EVENT_PARAM lpSearchEventParam) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextEvent(int lSearchHandle,
                                     NET_DVR_SEARCH_EVENT_RET lpSearchEventRet) {
        return 0;
    }

    @Override
    public int NET_DVR_FindFileByEvent_V50(int lUserID,
                                           NET_DVR_SEARCH_EVENT_PARAM_V50 lpSearchEventParam) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextEvent_V50(int lFindHandle,
                                         NET_DVR_SEARCH_EVENT_RET_V50 lpSearchEventRet) {
        return 0;
    }

    @Override
    public int NET_DVR_Upgrade(int lUserID,
                               String sFileName) {
        return 0;
    }

    @Override
    public int NET_DVR_GetUpgradeState(int lUpgradeHandle) {
        return 0;
    }

    @Override
    public int NET_DVR_GetUpgradeProgress(int lUpgradeHandle) {
        return 0;
    }

    @Override
    public boolean NET_DVR_CloseUpgradeHandle(int lUpgradeHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetNetworkEnvironment(int dwEnvironmentLevel) {
        return true;
    }

    @Override
    public int NET_DVR_FormatDisk(int lUserID,
                                  int lDiskNumber) {
        return 0;
    }

    @Override
    public boolean NET_DVR_GetFormatProgress(int lFormatHandle,
                                             IntByReference pCurrentFormatDisk,
                                             IntByReference pCurrentDiskPos,
                                             IntByReference pFormatStatic) {
        return true;
    }

    @Override
    public boolean NET_DVR_CloseFormatHandle(int lFormatHandle) {
        return true;
    }

    @Override
    public int NET_DVR_SetupAlarmChan(int lUserID) {
        return 0;
    }

    @Override
    public boolean NET_DVR_CloseAlarmChan(int lAlarmHandle) {
        return true;
    }

    @Override
    public int NET_DVR_SetupAlarmChan_V30(int lUserID) {
        return 0;
    }

    @Override
    public int NET_DVR_SetupAlarmChan_V41(int lUserID,
                                          NET_DVR_SETUPALARM_PARAM lpSetupParam) {
        return 0;
    }

    @Override
    public int NET_DVR_SetupAlarmChan_V50(int iUserID,
                                          NET_DVR_SETUPALARM_PARAM_V50 lpSetupParam,
                                          Pointer pSub,
                                          int dwSubSize) {
        return 0;
    }

    @Override
    public boolean NET_DVR_CloseAlarmChan_V30(int lAlarmHandle) {
        return true;
    }

    @Override
    public int NET_DVR_StartVoiceCom(int lUserID,
                                     FVoiceDataCallBack fVoiceDataCallBack,
                                     int dwUser) {
        return 0;
    }

    @Override
    public int NET_DVR_StartVoiceCom_V30(int lUserID,
                                         int dwVoiceChan,
                                         boolean bNeedCBNoEncData,
                                         FVoiceDataCallBack_V30 fVoiceDataCallBack,
                                         Pointer pUser) {
        return 0;
    }

    @Override
    public boolean NET_DVR_SetVoiceComClientVolume(int lVoiceComHandle,
                                                   short wVolume) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopVoiceCom(int lVoiceComHandle) {
        return true;
    }

    @Override
    public int NET_DVR_StartVoiceCom_MR(int lUserID,
                                        FVoiceDataCallBack_MR fVoiceDataCallBack,
                                        int dwUser) {
        return 0;
    }

    @Override
    public int NET_DVR_StartVoiceCom_MR_V30(int lUserID,
                                            int dwVoiceChan,
                                            FVoiceDataCallBack_MR_V30 fVoiceDataCallBack,
                                            Pointer pUser) {
        return 0;
    }

    @Override
    public boolean NET_DVR_VoiceComSendData(int lVoiceComHandle,
                                            byte[] pSendBuf,
                                            int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientAudioStart() {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientAudioStart_V30(FVoiceDataCallBack2 fVoiceDataCallBack2,
                                                Pointer pUser) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientAudioStop() {
        return true;
    }

    @Override
    public boolean NET_DVR_AddDVR(int lUserID) {
        return true;
    }

    @Override
    public int NET_DVR_AddDVR_V30(int lUserID,
                                  int dwVoiceChan) {
        return 0;
    }

    @Override
    public boolean NET_DVR_DelDVR(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_DelDVR_V30(int lVoiceHandle) {
        return true;
    }

    @Override
    public int NET_DVR_SerialStart(int lUserID,
                                   int lSerialPort,
                                   FSerialDataCallBack fSerialDataCallBack,
                                   int dwUser) {
        return 0;
    }

    @Override
    public int NET_DVR_SerialStart_V40(int lUserID,
                                       Pointer lpInBuffer,
                                       int dwInBufferSize,
                                       FSerialDataCallBack_V40 fSerialDataCallBack_V40,
                                       Pointer pUser) {
        return 0;
    }

    @Override
    public boolean NET_DVR_SerialSend(int lSerialHandle,
                                      int lChannel,
                                      byte[] pSendBuf,
                                      int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_SerialStop(int lSerialHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_SendTo232Port(int lUserID,
                                         String pSendBuf,
                                         int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_SendToSerialPort(int lUserID,
                                            int dwSerialPort,
                                            int dwSerialIndex,
                                            String pSendBuf,
                                            int dwBufSize) {
        return true;
    }

    @Override
    public Pointer NET_DVR_InitG722Encoder(NET_DVR_AUDIOENC_INFO enc_info) {
        return null;
    }

    @Override
    public boolean NET_DVR_EncodeG722Frame(Pointer handle,
                                           NET_DVR_AUDIOENC_PROCESS_PARAM param) {
        return true;
    }

    @Override
    public void NET_DVR_ReleaseG722Encoder(Pointer pEncodeHandle) {

    }

    @Override
    public Pointer NET_DVR_InitG722Decoder() {
        return null;
    }

    @Override
    public boolean NET_DVR_DecodeG722Frame(Pointer handle,
                                           NET_DVR_AUDIODEC_PROCESS_PARAM param) {
        return true;
    }

    @Override
    public void NET_DVR_ReleaseG722Decoder(Pointer pDecHandle) {

    }

    @Override
    public Pointer NET_DVR_InitG711Encoder(Pointer enc_info) {
        return null;
    }

    @Override
    public boolean NET_DVR_EncodeG711Frame(Pointer handle,
                                           NET_DVR_AUDIOENC_PROCESS_PARAM p_enc_proc_param) {
        return true;
    }

    @Override
    public boolean NET_DVR_ReleaseG711Encoder(Pointer pEncodeHandle) {
        return true;
    }

    @Override
    public Pointer NET_DVR_InitG711Decoder() {
        return null;
    }

    @Override
    public boolean NET_DVR_DecodeG711Frame(Pointer handle,
                                           NET_DVR_AUDIODEC_PROCESS_PARAM p_dec_proc_param) {
        return true;
    }

    @Override
    public boolean NET_DVR_ReleaseG711Decoder(Pointer pDecHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClickKey(int lUserID,
                                    int lKeyIndex) {
        return true;
    }

    @Override
    public boolean NET_DVR_StartDVRRecord(int lUserID,
                                          int lChannel,
                                          int lRecordType) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopDVRRecord(int lUserID,
                                         int lChannel) {
        return true;
    }

    @Override
    public boolean NET_DVR_InitDevice_Card(IntByReference pDeviceTotalChan) {
        return true;
    }

    @Override
    public boolean NET_DVR_ReleaseDevice_Card() {
        return true;
    }

    @Override
    public boolean NET_DVR_InitDDraw_Card(int hParent,
                                          int colorKey) {
        return true;
    }

    @Override
    public boolean NET_DVR_ReleaseDDraw_Card() {
        return true;
    }

    @Override
    public int NET_DVR_RealPlay_Card(int lUserID,
                                     NET_DVR_CARDINFO lpCardInfo,
                                     int lChannelNum) {
        return 0;
    }

    @Override
    public boolean NET_DVR_ResetPara_Card(int lRealHandle,
                                          NET_DVR_DISPLAY_PARA lpDisplayPara) {
        return true;
    }

    @Override
    public boolean NET_DVR_RefreshSurface_Card() {
        return true;
    }

    @Override
    public boolean NET_DVR_ClearSurface_Card() {
        return true;
    }

    @Override
    public boolean NET_DVR_RestoreSurface_Card() {
        return true;
    }

    @Override
    public boolean NET_DVR_OpenSound_Card(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_CloseSound_Card(int lRealHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetVolume_Card(int lRealHandle,
                                          short wVolume) {
        return true;
    }

    @Override
    public boolean NET_DVR_AudioPreview_Card(int lRealHandle,
                                             boolean bEnable) {
        return true;
    }

    @Override
    public int NET_DVR_GetCardLastError_Card() {
        return 0;
    }

    @Override
    public Pointer NET_DVR_GetChanHandle_Card(int lRealHandle) {
        return null;
    }

    @Override
    public boolean NET_DVR_CapturePicture_Card(int lRealHandle,
                                               String sPicFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetSerialNum_Card(int lChannelNum,
                                             IntByReference pDeviceSerialNo) {
        return true;
    }

    @Override
    public int NET_DVR_FindDVRLog(int lUserID,
                                  int lSelectMode,
                                  int dwMajorType,
                                  int dwMinorType,
                                  NET_DVR_TIME lpStartTime,
                                  NET_DVR_TIME lpStopTime) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextLog(int lLogHandle,
                                   NET_DVR_LOG lpLogData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_FindLogClose(int lLogHandle) {
        return true;
    }

    @Override
    public int NET_DVR_FindDVRLog_V30(int lUserID,
                                      int lSelectMode,
                                      int dwMajorType,
                                      int dwMinorType,
                                      NET_DVR_TIME lpStartTime,
                                      NET_DVR_TIME lpStopTime,
                                      boolean bOnlySmart) {
        return 0;
    }

    @Override
    public int NET_DVR_FindNextLog_V30(int lLogHandle,
                                       NET_DVR_LOG_V30 lpLogData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_FindLogClose_V30(int lLogHandle) {
        return true;
    }

    @Override
    public int NET_DVR_FindFileByCard(int lUserID,
                                      int lChannel,
                                      int dwFileType,
                                      int nFindType,
                                      String sCardNumber,
                                      NET_DVR_TIME lpStartTime,
                                      NET_DVR_TIME lpStopTime) {
        return 0;
    }

    @Override
    public boolean NET_DVR_CaptureJPEGPicture(int lUserID,
                                              int lChannel,
                                              NET_DVR_JPEGPARA lpJpegPara,
                                              byte[] sPicFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_CaptureJPEGPicture_NEW(int lUserID,
                                                  int lChannel,
                                                  NET_DVR_JPEGPARA lpJpegPara,
                                                  Pointer sJpegPicBuffer,
                                                  int dwPicSize,
                                                  IntByReference lpSizeReturned) {
        return true;
    }

    @Override
    public boolean NET_DVR_CaptureJPEGPicture_WithAppendData(int lUserID,
                                                             int iChannelNum,
                                                             NET_DVR_JPEGPICTURE_WITH_APPENDDATA m_strJpegWithAppendData) {
        return true;
    }

    @Override
    public int NET_DVR_GetRealPlayerIndex(int lRealHandle) {
        return 0;
    }

    @Override
    public int NET_DVR_GetPlayBackPlayerIndex(int lPlayHandle) {
        return 0;
    }

    @Override
    public boolean NET_DVR_SetScaleCFG(int lUserID,
                                       int dwScale) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetScaleCFG(int lUserID,
                                       IntByReference lpOutScale) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetScaleCFG_V30(int lUserID,
                                           NET_DVR_SCALECFG pScalecfg) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetScaleCFG_V30(int lUserID,
                                           NET_DVR_SCALECFG pScalecfg) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetATMPortCFG(int lUserID,
                                         short wATMPort) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetATMPortCFG(int lUserID,
                                         ShortByReference LPOutATMPort) {
        return true;
    }

    @Override
    public boolean NET_DVR_InitDDrawDevice() {
        return true;
    }

    @Override
    public boolean NET_DVR_ReleaseDDrawDevice() {
        return true;
    }

    @Override
    public int NET_DVR_GetDDrawDeviceTotalNums() {
        return 0;
    }

    @Override
    public boolean NET_DVR_SetDDrawDevice(int lPlayPort,
                                          int nDeviceNum) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZSelZoomIn(int lRealHandle,
                                        NET_DVR_POINT_FRAME pStruPointFrame) {
        return true;
    }

    @Override
    public boolean NET_DVR_PTZSelZoomIn_EX(int lUserID,
                                           int lChannel,
                                           NET_DVR_POINT_FRAME pStruPointFrame) {
        return true;
    }

    @Override
    public boolean NET_DVR_FocusOnePush(int lUserID,
                                        int lChannel) {
        return true;
    }

    @Override
    public boolean NET_DVR_StartDecode(int lUserID,
                                       int lChannel,
                                       NET_DVR_DECODERINFO lpDecoderinfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopDecode(int lUserID,
                                      int lChannel) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDecoderState(int lUserID,
                                           int lChannel,
                                           NET_DVR_DECODERSTATE lpDecoderState) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDecInfo(int lUserID,
                                      int lChannel,
                                      NET_DVR_DECCFG lpDecoderinfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDecInfo(int lUserID,
                                      int lChannel,
                                      NET_DVR_DECCFG lpDecoderinfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDecTransPort(int lUserID,
                                           NET_DVR_PORTCFG lpTransPort) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDecTransPort(int lUserID,
                                           NET_DVR_PORTCFG lpTransPort) {
        return true;
    }

    @Override
    public boolean NET_DVR_DecPlayBackCtrl(int lUserID,
                                           int lChannel,
                                           int dwControlCode,
                                           int dwInValue,
                                           IntByReference LPOutValue,
                                           NET_DVR_PLAYREMOTEFILE lpRemoteFileInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_StartDecSpecialCon(int lUserID,
                                              int lChannel,
                                              NET_DVR_DECCHANINFO lpDecChanInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_StopDecSpecialCon(int lUserID,
                                             int lChannel,
                                             NET_DVR_DECCHANINFO lpDecChanInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_DecCtrlDec(int lUserID,
                                      int lChannel,
                                      int dwControlCode) {
        return true;
    }

    @Override
    public boolean NET_DVR_DecCtrlScreen(int lUserID,
                                         int lChannel,
                                         int dwControl) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDecCurLinkStatus(int lUserID,
                                               int lChannel,
                                               NET_DVR_DECSTATUS lpDecStatus) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixStartDynamic(int lUserID,
                                              int dwDecChanNum,
                                              NET_DVR_MATRIX_DYNAMIC_DEC lpDynamicInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixStopDynamic(int lUserID,
                                             int dwDecChanNum) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetDecChanInfo(int lUserID,
                                                int dwDecChanNum,
                                                NET_DVR_MATRIX_DEC_CHAN_INFO lpInter) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetLoopDecChanInfo(int lUserID,
                                                    int dwDecChanNum,
                                                    NET_DVR_MATRIX_LOOP_DECINFO lpInter) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetLoopDecChanInfo(int lUserID,
                                                    int dwDecChanNum,
                                                    NET_DVR_MATRIX_LOOP_DECINFO lpInter) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetLoopDecChanEnable(int lUserID,
                                                      int dwDecChanNum,
                                                      int dwEnable) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetLoopDecChanEnable(int lUserID,
                                                      int dwDecChanNum,
                                                      IntByReference lpdwEnable) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetLoopDecEnable(int lUserID,
                                                  IntByReference lpdwEnable) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetDecChanEnable(int lUserID,
                                                  int dwDecChanNum,
                                                  int dwEnable) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetDecChanEnable(int lUserID,
                                                  int dwDecChanNum,
                                                  IntByReference lpdwEnable) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetDecChanStatus(int lUserID,
                                                  int dwDecChanNum,
                                                  NET_DVR_MATRIX_DEC_CHAN_STATUS lpInter) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixStartDynamic_V41(int lUserID,
                                                  int dwDecChanNum,
                                                  Pointer lpDynamicInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetLoopDecChanInfo_V41(int lUserID,
                                                        int dwDecChanNum,
                                                        NET_DVR_MATRIX_LOOP_DECINFO_V41 lpOuter) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetLoopDecChanInfo_V41(int lUserID,
                                                        int dwDecChanNum,
                                                        NET_DVR_MATRIX_LOOP_DECINFO_V41 lpInter) {
        return true;
    }

    @Override
    public int NET_DVR_MatrixStartPassiveDecode(int lUserID,
                                                int dwDecChanNum,
                                                Pointer lpPassiveMode) {
        return 0;
    }

    @Override
    public boolean NET_DVR_MatrixSendData(int lPassiveHandle,
                                          Pointer pSendBuf,
                                          int dwBufSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixStopPassiveDecode(int lPassiveHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetTranInfo(int lUserID,
                                             NET_DVR_MATRIX_TRAN_CHAN_CONFIG lpTranInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetTranInfo(int lUserID,
                                             NET_DVR_MATRIX_TRAN_CHAN_CONFIG lpTranInfo) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetRemotePlay(int lUserID,
                                               int dwDecChanNum,
                                               NET_DVR_MATRIX_DEC_REMOTE_PLAY lpInter) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixSetRemotePlayControl(int lUserID,
                                                      int dwDecChanNum,
                                                      int dwControlCode,
                                                      int dwInValue,
                                                      IntByReference LPOutValue) {
        return true;
    }

    @Override
    public boolean NET_DVR_MatrixGetRemotePlayStatus(int lUserID,
                                                     int dwDecChanNum,
                                                     NET_DVR_MATRIX_DEC_REMOTE_PLAY_STATUS lpOuter) {
        return true;
    }

    @Override
    public boolean NET_DVR_RefreshPlay(int lPlayHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_RestoreConfig(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_SaveConfig(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_RebootDVR(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_ShutDownDVR(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDeviceConfig(int lUserID,
                                           int dwCommand,
                                           int dwCount,
                                           Pointer lpInBuffer,
                                           int dwInBufferSize,
                                           Pointer lpStatusList,
                                           Pointer lpOutBuffer,
                                           int dwOutBufferSize) {
        if (dwCommand == NET_DVR_GET_MONTHLY_RECORD_DISTRIBUTION) {
            byte[] byRecordDistribution = {0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1};
            lpOutBuffer.write(4, byRecordDistribution, 0, byRecordDistribution.length);
        }

        return true;
    }

    @Override
    public boolean NET_DVR_SetDeviceConfig(int lUserID,
                                           int dwCommand,
                                           int dwCount,
                                           Pointer lpInBuffer,
                                           int dwInBufferSize,
                                           Pointer lpStatusList,
                                           Pointer lpInParamBuffer,
                                           int dwInParamBufferSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetDeviceConfigEx(int lUserID,
                                             int dwCommand,
                                             int dwCount,
                                             Pointer lpInParam,
                                             Pointer lpOutParam) {
        return true;
    }

    /**
     * 获取RS485配置（模拟）
     *
     * @param lUserID
     * @param dwCommand
     * @param lChannel
     * @param lpOutBuffer
     * @param dwOutBufferSize
     * @param lpBytesReturned
     * @return 是否成功
     * @see HCNetSDK.NET_DVR_ALARM_RS485CFG
     */
    @Override
    public boolean NET_DVR_GetDVRConfig(int lUserID,
                                        int dwCommand,
                                        int lChannel,
                                        Pointer lpOutBuffer,
                                        int dwOutBufferSize,
                                        IntByReference lpBytesReturned) {
        if (dwCommand == NET_DVR_GET_ALARM_RS485CFG) {
            NET_DVR_ALARM_RS485CFG cfg = new NET_DVR_ALARM_RS485CFG();
            byte[] bytes = "test-device-name".getBytes(StandardCharsets.UTF_8);
            System.arraycopy(bytes, 0, cfg.sDeviceName, 0, bytes.length);
            cfg.wDeviceType = (short) 1;
            cfg.wDeviceProtocol = (short) 1;
            cfg.dwBaudRate = 9;
            cfg.byDataBit = (byte) 0;
            cfg.byStopBit = (byte) 1;
            cfg.byParity = (byte) 2;
            cfg.byFlowcontrol = (byte) 0;
            cfg.byDuplex = (byte) 0;
            cfg.byWorkMode = (byte) 4;
            cfg.byChannel = (byte) 4;
            cfg.bySerialType = (byte) 0;
            cfg.byMode = (byte) 3;
            cfg.byOutputDataType = (byte) 3;
            cfg.byAddress = (byte) 1;
            int offset = 4;
            // 前端设备名称
            lpOutBuffer.write(offset, cfg.sDeviceName, 0, cfg.sDeviceName.length);
            offset += NAME_LEN;
            // 前端设备类型,通过NET_DVR_GetDeviceTypeList获取
            lpOutBuffer.setShort(offset, cfg.wDeviceType);
            offset += 2;
            // 前端设备协议
            lpOutBuffer.setShort(offset, cfg.wDeviceProtocol);
            offset += 2;
            // 波特率(bps)：0-50，1-75，2-110，3-150，4-300，5-600，6-1200，7-2400，8-4800，9-9600，10-19200，11-38400，12-57600，13-76800，14-115.2k
            lpOutBuffer.setInt(offset, cfg.dwBaudRate);
            offset += 4;
            // 数据有几位：0-5位，1-6位，2-7位，3-8位
            lpOutBuffer.setByte(++offset, cfg.byDataBit);
            // 停止位：0-1位，1-2位
            lpOutBuffer.setByte(++offset, cfg.byStopBit);
            // 是否校验：0-无校验，1-奇校验，2-偶校验
            lpOutBuffer.setByte(++offset, cfg.byParity);
            // 是否流控：0-无，1-软流控,2-硬流控
            lpOutBuffer.setByte(++offset, cfg.byFlowcontrol);
            // 0 - 半双工1- 全双工  只有通道1可以是全双工其他都只能是半双工
            lpOutBuffer.setByte(++offset, cfg.byDuplex);
            // 工作模式 0-控制台 1-透明通道,2-梯控，3-读卡器,4-门禁安全模块,0xfe-自定义，0xff-禁用
            lpOutBuffer.setByte(++offset, cfg.byWorkMode);
            // 485通道号
            lpOutBuffer.setByte(++offset, cfg.byChannel);
            // 串口类型: 0--485, 1--232
            lpOutBuffer.setByte(++offset, cfg.bySerialType);
            // 模式 0-连接读卡器 1-连接客户端 2-连接扩展模块 3-连接门禁主机 4-连接梯控主机  0xff-禁用
            lpOutBuffer.setByte(++offset, cfg.byMode);
            // 0-无效，1-输出卡号，2-输出工号
            lpOutBuffer.setByte(++offset, cfg.byOutputDataType);
            // 串口地址
            lpOutBuffer.setByte(++offset, cfg.byAddress);

        }
        return true;
    }

    @Override
    public boolean NET_DVR_SetDVRConfig(int lUserID,
                                        int dwCommand,
                                        int lChannel,
                                        Pointer lpInBuffer,
                                        int dwInBufferSize) {
        if (dwCommand == NET_DVR_GET_ALARM_RS485CFG) {
            int offset = 4;
            // 前端设备名称
            lpInBuffer.read(offset, new byte[0], 0, NAME_LEN);
            offset += NAME_LEN;
            // 前端设备类型,通过NET_DVR_GetDeviceTypeList获取
            lpInBuffer.getShort(offset);
            offset += 2;
            // 前端设备协议
            lpInBuffer.getShort(offset);
            offset += 2;
            // 波特率(bps)：0-50，1-75，2-110，3-150，4-300，5-600，6-1200，7-2400，8-4800，9-9600，10-19200，11-38400，12-57600，13-76800，14-115.2k
            lpInBuffer.getInt(offset);
            offset += 4;
            // 数据有几位：0-5位，1-6位，2-7位，3-8位
            lpInBuffer.getByte(++offset);
            // 停止位：0-1位，1-2位
            lpInBuffer.getByte(++offset);
            // 是否校验：0-无校验，1-奇校验，2-偶校验
            lpInBuffer.getByte(++offset);
            // 是否流控：0-无，1-软流控,2-硬流控
            lpInBuffer.getByte(++offset);
            // 0 - 半双工1- 全双工  只有通道1可以是全双工其他都只能是半双工
            lpInBuffer.getByte(++offset);
            // 工作模式 0-控制台 1-透明通道,2-梯控，3-读卡器,4-门禁安全模块,0xfe-自定义，0xff-禁用
            lpInBuffer.getByte(++offset);
            // 485通道号
            lpInBuffer.getByte(++offset);
            // 串口类型: 0--485, 1--232
            lpInBuffer.getByte(++offset);
            // 模式 0-连接读卡器 1-连接客户端 2-连接扩展模块 3-连接门禁主机 4-连接梯控主机  0xff-禁用
            lpInBuffer.getByte(++offset);
            // 0-无效，1-输出卡号，2-输出工号
            lpInBuffer.getByte(++offset);
            // 串口地址
            lpInBuffer.getByte(++offset);
        }
        return true;
    }

    @Override
    public boolean NET_DVR_GetSTDConfig(int lUserID,
                                        int dwCommand,
                                        NET_DVR_STD_CONFIG lpConfigParam) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetSTDConfig(int lUserID,
                                        int dwCommand,
                                        NET_DVR_STD_CONFIG lpConfigParam) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDVRWorkState_V30(int lUserID,
                                               NET_DVR_WORKSTATE_V30 lpWorkState) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDVRWorkState(int lUserID,
                                           NET_DVR_WORKSTATE lpWorkState) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetVideoEffect(int lUserID,
                                          int lChannel,
                                          int dwBrightValue,
                                          int dwContrastValue,
                                          int dwSaturationValue,
                                          int dwHueValue) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetVideoEffect(int lUserID,
                                          int lChannel,
                                          IntByReference pBrightValue,
                                          IntByReference pContrastValue,
                                          IntByReference pSaturationValue,
                                          IntByReference pHueValue) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientGetframeformat(int lUserID,
                                                NET_DVR_FRAMEFORMAT lpFrameFormat) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientSetframeformat(int lUserID,
                                                NET_DVR_FRAMEFORMAT lpFrameFormat) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientGetframeformat_V30(int lUserID,
                                                    NET_DVR_FRAMEFORMAT_V30 lpFrameFormat) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientSetframeformat_V30(int lUserID,
                                                    NET_DVR_FRAMEFORMAT_V30 lpFrameFormat) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetAlarmOut_V30(int lUserID,
                                           NET_DVR_ALARMOUTSTATUS_V30 lpAlarmOutState) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetAlarmOut(int lUserID,
                                       NET_DVR_ALARMOUTSTATUS lpAlarmOutState) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetAlarmOut(int lUserID,
                                       int lAlarmOutPort,
                                       int lAlarmOutStatic) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientSetVideoEffect(int lRealHandle,
                                                int dwBrightValue,
                                                int dwContrastValue,
                                                int dwSaturationValue,
                                                int dwHueValue) {
        return true;
    }

    @Override
    public boolean NET_DVR_ClientGetVideoEffect(int lRealHandle,
                                                IntByReference pBrightValue,
                                                IntByReference pContrastValue,
                                                IntByReference pSaturationValue,
                                                IntByReference pHueValue) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetConfigFile(int lUserID,
                                         String sFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetConfigFile(int lUserID,
                                         String sFileName) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetConfigFile_V30(int lUserID,
                                             String sOutBuffer,
                                             int dwOutSize,
                                             IntByReference pReturnSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetConfigFile_EX(int lUserID,
                                            String sOutBuffer,
                                            int dwOutSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetConfigFile_EX(int lUserID,
                                            String sInBuffer,
                                            int dwInSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetLogToFile(int bLogEnable,
                                        String strLogDir,
                                        boolean bAutoDel) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetSDKState(NET_DVR_SDKSTATE pSDKState) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetSDKAbility(NET_DVR_SDKABL pSDKAbl) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetPTZProtocol(int lUserID,
                                          NET_DVR_PTZCFG pPtzcfg) {
        return true;
    }

    @Override
    public boolean NET_DVR_LockPanel(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_UnLockPanel(int lUserID) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetRtspConfig(int lUserID,
                                         int dwCommand,
                                         NET_DVR_RTSPCFG lpInBuffer,
                                         int dwInBufferSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetRtspConfig(int lUserID,
                                         int dwCommand,
                                         NET_DVR_RTSPCFG lpOutBuffer,
                                         int dwOutBufferSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_ContinuousShoot(int lUserID,
                                           NET_DVR_SNAPCFG lpInter) {
        return true;
    }

    @Override
    public boolean NET_DVR_ManualSnap(int lUserID,
                                      NET_DVR_MANUALSNAP lpInter,
                                      NET_DVR_PLATE_RESULT lpOuter) {
        return true;
    }

    @Override
    public int NET_DVR_StartRemoteConfig(int lUserID,
                                         int dwCommand,
                                         Pointer lpInBuffer,
                                         int dwInBufferLen,
                                         FRemoteConfigCallBack cbStateCallBack,
                                         Pointer pUserData) {
        return 0;
    }

    @Override
    public boolean NET_DVR_SendRemoteConfig(int lHandle,
                                            int dwDataType,
                                            Pointer pSendBuf,
                                            int dwBufSize) {
        return true;
    }

    @Override
    public int NET_DVR_GetNextRemoteConfig(int lHandle,
                                           Pointer lpOutBuff,
                                           int dwOutBuffSize) {
        return 0;
    }

    @Override
    public int NET_DVR_SendWithRecvRemoteConfig(int lHandle,
                                                Pointer lpInBuff,
                                                int dwInBuffSize,
                                                Pointer lpOutBuff,
                                                int dwOutBuffSize,
                                                IntByReference dwOutDataLen) {
        return 0;
    }

    @Override
    public boolean NET_DVR_StopRemoteConfig(int lHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_RemoteControl(int lUserID,
                                         int dwCommand,
                                         Pointer lpInBuffer,
                                         int dwInBufferSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_STDXMLConfig(int lUserID,
                                        NET_DVR_XML_CONFIG_INPUT lpInputParam,
                                        NET_DVR_XML_CONFIG_OUTPUT lpOutputParam) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetSTDAbility(int lUserID,
                                         int dwAbilityType,
                                         NET_DVR_STD_ABILITY lpAbilityParam) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetDeviceAbility(int lUserID,
                                            int dwAbilityType,
                                            Pointer pInBuf,
                                            int dwInLength,
                                            Pointer pOutBuf,
                                            int dwOutLength) {
        return true;
    }

    @Override
    public boolean NET_DVR_ControlGateway(int lUserID,
                                          int lGatewayIndex,
                                          int dwStaic) {
        return true;
    }

    @Override
    public boolean NET_DVR_InquestStartCDW_V30(int lUserID,
                                               NET_DVR_INQUEST_ROOM lpInquestRoom,
                                               boolean bNotBurn) {
        return true;
    }

    @Override
    public boolean NET_DVR_InquestStopCDW_V30(int lUserID,
                                              NET_DVR_INQUEST_ROOM lpInquestRoom,
                                              boolean bCancelWrite) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetArrayList(int lUserID,
                                        NET_DVR_ARRAY_LIST lpArrayList) {
        return true;
    }

    @Override
    public int NET_DVR_InquestResumeEvent(int lUserID,
                                          NET_DVR_INQUEST_RESUME_EVENT lpResumeEvent) {
        return 0;
    }

    @Override
    public boolean NET_DVR_InquestGetResumeProgress(int lHandle,
                                                    IntByReference pState) {
        return true;
    }

    @Override
    public boolean NET_DVR_InquestStopResume(int lHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetLocalIP(byte[] strIP,
                                      IntByReference pValidNum,
                                      boolean pEnableBind) {
        return true;
    }

    @Override
    public boolean NET_DVR_SetValidIP(int dwIPIndex,
                                      boolean bEnableBind) {
        return true;
    }

    @Override
    public boolean NET_DVR_AlarmHostAssistantControl(int lUserID,
                                                     int dwType,
                                                     int dwNumber,
                                                     int dwCmdParam) {
        return true;
    }

    @Override
    public boolean NET_DVR_GetPlanList(int lUserID,
                                       int dwDevNum,
                                       NET_DVR_PLAN_LIST lpPlanList) {
        return true;
    }

    @Override
    public int NET_DVR_UploadFile_V40(int lUserID,
                                      int dwUploadType,
                                      Pointer lpInBuffer,
                                      int dwInBufferSize,
                                      String sFileName,
                                      Pointer lpOutBuffer,
                                      int dwOutBufferSize) {
        return 0;
    }

    @Override
    public int NET_DVR_UploadSend(int lUserID,
                                  NET_DVR_SEND_PARAM_IN pstruSendParamIN,
                                  Pointer lpOutBuffer) {
        return 0;
    }

    @Override
    public int NET_DVR_GetUploadState(int lUploadHandle,
                                      Pointer pProgress) {
        return 0;
    }

    @Override
    public boolean NET_DVR_GetUploadResult(int lUploadHandle,
                                           Pointer lpOutBuffer,
                                           int dwOutBufferSize) {
        return true;
    }

    @Override
    public boolean NET_DVR_UploadClose(int lUploadHandle) {
        return true;
    }

    @Override
    public int NET_DVR_StartNetworkFlowTest(int lUserID,
                                            NET_DVR_FLOW_TEST_PARAM pFlowTest,
                                            FLOWTESTCALLBACK fFlowTestCallback,
                                            Pointer pUser) {
        return 0;
    }

    @Override
    public boolean NET_DVR_StopNetworkFlowTest(int lHandle) {
        return true;
    }

    @Override
    public boolean NET_DVR_InquiryRecordTimeSpan(int lUserID,
                                                 int dwChannel,
                                                 NET_DVR_RECORD_TIME_SPAN_INQUIRY lpInquiry,
                                                 NET_DVR_RECORD_TIME_SPAN lpResult) {
        return true;
    }

    @Override
    public boolean NET_DVR_StartGetDevState(NET_DVR_CHECK_DEV_STATE pParams) {
        return true;
    }

    @Override
    public int NET_DVR_GetVehicleGpsInfo(int lUserID,
                                         NET_DVR_GET_GPS_DATA_PARAM lpGPSDataParam,
                                         fGPSDataCallback cbGPSDataCallBack,
                                         Pointer pUser) {
        return 0;
    }
}
