package kilpatrick.paul.mediareleaseservice.domain;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by paulk on 21/09/15.
 */
public class MediaRelease implements Serializable {

    long serialVersionUID = 1L;

    long id;
    String title;
    String snippet;
    String url;
    boolean favourited;
    boolean hidden;
    Date created;
    boolean visited;

    public MediaRelease(JSONObject json) throws JSONException {
        this.id = json.getLong("id");
        this.title = json.getString("title");
        this.snippet = json.getString("snippet");
        this.url = json.getString("url");
    }

    public MediaRelease() {

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFavourited() {
        return favourited;
    }

    public void setFavourited(boolean favourited) {
        this.favourited = favourited;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
