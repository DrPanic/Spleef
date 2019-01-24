package fr.naruse.spleef.util;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.Main;
import org.bukkit.Bukkit;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Updater {
    private Main pl;
    public Updater(Main main) {
        this.pl = main;
    }

    public boolean update() {
        try {
            if(needToDownload("https://raw.githubusercontent.com/NaruseII/Spleef/master/src/plugin.yml")){
                download("https://download1083.mediafire.com/g2fhfor0az2g/0zxuoqplh0kvtwm/Spleef.jar",  new File(Bukkit.getWorldContainer()+"/plugins", "Spleef.jar"));
                return true;
            }else{
                Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §aNo new version available.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean needToDownload(String host) throws IOException {
        List<String> words = Lists.newArrayList();
        BufferedReader r = new BufferedReader(new InputStreamReader(new URL(host).openStream()));
        String s;
        while ((s = r.readLine()) != null) {
            words.add(s);
            System.out.println(s);
        }
        return !words.contains(pl.getDescription().getVersion());
    }

    private File download(String host, File dest) throws IOException, NullPointerException{
        InputStream is = null;
        OutputStream os = null;
        try {
            Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §aDownloading "+dest.getName()+"...");
            if(dest.exists()) dest.delete();
            URL url = new URL(host);
            URLConnection connection = url.openConnection();
            int fileLength = connection.getContentLength();
            if (fileLength == -1) {
                Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l]§4 Invalide URL or file.");
                return null;
            }
            is = connection.getInputStream();
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
            Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §aDownload complete");
        }
        return dest;
    }
}
