package me.modmuss50.assets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AssetValidator {

	private static final Gson GSON = new GsonBuilder().create();

	public static void validate(File assetsDir) throws Exception {
		final String mcVersion = getMinecraftVersion();
		JsonObject assets = getAssets(mcVersion, assetsDir);
		if (assets != null) {
			downloadAssets(assets, assetsDir);
		}
	}

	private static void downloadAssets(JsonObject jsonObject, File assetsDir) throws IOException {
		JsonObject objects = jsonObject.getAsJsonObject("objects");
		for (Map.Entry<String, JsonElement> entry : objects.entrySet()) {
			String assetName = entry.getKey();
			String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();

			String dirName = hash.substring(0, 2);
			File file = new File(assetsDir, "objects/" + dirName + "/" + hash);

			//TODO check hash?
			if (!file.exists()) {
				String url = "http://resources.download.minecraft.net/" + dirName + "/" + hash;
				System.out.println("Downloading: " + assetName);
				FileUtils.copyURLToFile(new URL(url), file);
			}
		}
	}

	private static JsonObject getAssets(String mcVersion, File assetsDir) throws IOException {
		JsonObject versionMeta = getVersionMeta(mcVersion);
		if (versionMeta != null && versionMeta.has("assetIndex")) {
			String indexUrl = versionMeta.get("assetIndex").getAsJsonObject().get("url").getAsString();
			File indexFile = new File(assetsDir, "indexes/" + getIndexFileName(versionMeta) + ".json");
			if (!indexFile.exists()) {
				System.out.println("Downloading: " + indexFile.getName());
				FileUtils.copyURLToFile(new URL(indexUrl), indexFile);
			}
			return getJsonObject(indexUrl);
		}
		return null;
	}

	private static String getIndexFileName(JsonObject jsonObject) {
		String version = jsonObject.get("id").getAsString();
		String id = jsonObject.get("assets").getAsString();
		return id.equals(version) ? version : version + "-" + id;
	}

	private static JsonObject getVersionMeta(String mcVersion) throws IOException {
		JsonObject launcherMeta = getJsonObject("https://launchermeta.mojang.com/mc/game/version_manifest.json");

		if (launcherMeta == null) {
			return null;
		}

		JsonArray versionArray = launcherMeta.getAsJsonArray("versions");
		for (int i = 0; i < versionArray.size(); i++) {
			JsonObject version = versionArray.get(i).getAsJsonObject();
			if (version.has("id") && version.get("id").getAsString().equals(mcVersion)) {
				return getJsonObject(version.get("url").getAsString());
			}
		}
		return null;
	}

	private static String getMinecraftVersion() {
		return SharedConstants.getGameVersion().getName();
	}

	private static JsonObject getJsonObject(String url) throws IOException {
		String json = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
		return GSON.fromJson(json, JsonObject.class);
	}
}
