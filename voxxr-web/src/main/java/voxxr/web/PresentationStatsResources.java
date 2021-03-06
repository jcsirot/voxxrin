package voxxr.web;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.Collections2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 1/28/12
 * Time: 8:51 PM
 */
public class PresentationStatsResources implements RestRouter.RequestHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params) throws IOException {
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            try {
                String eventId = params.get("eventId");
                String presentationId = params.get("presentationId");
                MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService("jsons");
                String cacheKey = "PresentationStats/" + eventId + "/" + presentationId;
                String prezStatsJsonStr = (String) memcacheService.get(cacheKey);
                if (prezStatsJsonStr != null) {
                    Rests.sendJson(prezStatsJsonStr, req, resp);
                    return;
                }

                JSONObject presStat = new JSONObject();
                presStat.put("id", presentationId);
                presStat.put("favorites", PresentationResources.countFavorites(presentationId));
                Collection<MyPresentation> favoritedBy = PresentationResources.involvedUsers(presentationId);
                presStat.put("involvedUsers", new JSONArray(Collections2.transform(favoritedBy, MyPresentation.TO_JSON)));
                prezStatsJsonStr = presStat.toString();
                memcacheService.put(cacheKey, prezStatsJsonStr, Expiration.byDeltaSeconds(15));
                Rests.sendJson(prezStatsJsonStr, req, resp);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


