
import java.io.*;
import java.util.*;

/**
 * @Copyright © 2021 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2021/12/15 11:46 AM
 * @author: sanbo
 */
public class SdkListParser {
    // {target:{arch:[api-level]}}
    private static Map<String, Map<String, Set<String>>> map = new HashMap<String, Map<String, Set<String>>>();

    public static void main(String[] args) {
        parserMac();
    }

    private static Map parserMac() {
//        List<String> lists = readForArray("mac.txt");
        List<String> lists = getRealResult("sdkmanager --list");
        for (String line : lists) {
            if (isEmpty(line) || isEmpty(line.trim())) {
                continue;
            }
            line = line.trim();
//            System.out.println(line);
            if (line.startsWith("system-images")) {
                prserLine(line);
            }
        }
        System.out.println(map);
        return map;
    }

    private static void prserLine(String line) {
//        System.out.println(line);
        if (line.contains(" ")) {
            String[] as = line.split("\\s+");
//            System.out.println(as[0]);
            parserInfo(as[0]);
        } else {
            System.err.println(line);
        }
    }

    private static void parserInfo(String info) {
        if (!info.contains(";")) {
            System.err.println(info);
            return;
        }
        //[system-images, android-29, google_apis_playstore, arm64-v8a]
        String[] ss = info.split(";");
        if (ss.length != 4) {
            System.err.println(info);
            return;
        }
        String apiLevel = ss[1].replaceAll("android-", "");
        String target = ss[2];
        String arch = ss[3];
        System.out.println(apiLevel + "-----" + target + "-----" + arch);

        Map<String, Set<String>> archAndApilevel = new HashMap<String, Set<String>>();
        if (map.containsKey(target)) {
            archAndApilevel = map.get(target);
        }
        Set<String> apiLevels = new HashSet<String>();
        if (archAndApilevel.containsKey(arch)) {
            apiLevels = archAndApilevel.get(arch);
            if (apiLevels.contains(apiLevel)) {
                return;
            }
        }
        apiLevels.add(apiLevel);
        archAndApilevel.put(arch, apiLevels);
        map.put(target, archAndApilevel);
    }


    /**
     * 读取文件内容,将文件内容按行分割以列表形式返回
     *
     * @param fileFullPathWithName 文件的全路径名称
     * @return
     */
    public static List<String> readForArray(String fileFullPathWithName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileFullPathWithName);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new ArrayList<String>(Arrays.asList(new String(buffer, "UTF-8").split("\n")));
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }
        return new ArrayList<String>();
    }

    private static void close(Closeable... obj) {
        if (obj != null && obj.length > 0) {
            for (Closeable close : obj) {
                try {
                    close.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }


    public static List<String> getRealResult(String... cmd) {
        List<String> arrResult = new ArrayList<String>();
        if (cmd == null || cmd.length == 0) {
            return arrResult;
        }
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        try {
            proc = Runtime.getRuntime().exec("sh");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            for (int i = 0; i < cmd.length; i++) {
                os.write(cmd[i].getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                arrResult.add(line);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            close(pos, ii, br, is, in, os);
        }

        return arrResult;
    }
}
