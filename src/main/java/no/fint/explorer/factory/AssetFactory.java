package no.fint.explorer.factory;

import no.fint.explorer.model.Asset;
import no.fint.explorer.model.SseOrg;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public final class AssetFactory {
    private final static String PROVIDER = "/provider";

    public static Asset toAsset(Map.Entry<String, List<SseOrg>> entry) {
        Asset asset = new Asset();

        asset.setId(entry.getKey());

        entry.getValue()
                .stream()
                .map(AssetFactory::toComponent)
                .forEach(asset.getComponents()::add);

        return asset;
    }

    private static Asset.Component toComponent(SseOrg sseOrg) {
        Asset.Component component = new Asset.Component();

        component.setId(getComponentId(sseOrg.getPath()));
        component.setTitle(getComponentTitle(sseOrg.getPath()));
        component.setClients(sseOrg.getClients());

        return component;
    }

    private static String getComponentId(String path) {
        return StringUtils.substringBetween(path, "/", PROVIDER).replaceAll("/", "-");
    }

    private static String getComponentTitle(String path) {
        String title = StringUtils.substringBetween(path, "/", PROVIDER).replaceAll("/", " ");

        return WordUtils.capitalize(title);
    }
}
