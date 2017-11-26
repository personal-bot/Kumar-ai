/**
 *  JsonPathTestService
 *  Copyright 6.05.2017 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.kumar.server.api.kumar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ai.kumar.json.JsonObjectWithDefault;
import ai.kumar.json.JsonPath;
import ai.kumar.server.APIException;
import ai.kumar.server.APIHandler;
import ai.kumar.server.AbstractAPIHandler;
import ai.kumar.server.Authorization;
import ai.kumar.server.BaseUserRole;
import ai.kumar.server.Query;
import ai.kumar.server.ServiceResponse;

/**
 * test a jsonpath
 * i.e.:
 * http://localhost:4000/kumar/jsonpath.json?json=%7B%22text%22:%22abc%22%7D&path=$.text
 * http://localhost:4000/kumar/jsonpath.json?path=$.Models[0]&url=https://www.carqueryapi.com/api/0.3/?callback=xx%26cmd=getModels%26make=ford
 */
public class JsonPathTestService extends AbstractAPIHandler implements APIHandler {
   
    private static final long serialVersionUID = 857847333032749879L;

    @Override
    public String getAPIPath() {
        return "/kumar/jsonpath.json";
    }

    @Override
    public BaseUserRole getMinimalBaseUserRole() {
        return BaseUserRole.ANONYMOUS;
    }

    @Override
    public JSONObject getDefaultPermissions(BaseUserRole baseUserRole) {
        return null;
    }

    @Override
    public ServiceResponse serviceImpl(Query post, HttpServletResponse response, Authorization rights, JsonObjectWithDefault permissions) throws APIException {

        post.setResponse(response, "application/javascript");

        String jsonString = post.get("json", "{}").trim();
        String url = post.get("url", "").trim();
        if (jsonString.equals("{}") && url.length() > 0) {
            // replace jsonString with content from web
            try {
                byte[] b = ConsoleService.loadData(url);
                jsonString = new String(b, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String path = post.get("path", "$").trim();
        
        JSONTokener tokener = new JSONTokener(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)));

        JSONObject testresult = new JSONObject(true);
        testresult.put("test", jsonString);
        testresult.put("path", path);
        try {
            JSONArray data = JsonPath.parse(tokener, path);
            testresult.put("data", data == null ? "error" : data);
        } catch (JSONException e) {
            testresult.put("data", "error: " + e.getMessage());
        }
        return new ServiceResponse(testresult);
    }

}
