/**
 *  MultipartConfigInjectionHandler
 *  Copyright 04.08.2017 by Chetan Kaushik, @dynamitechetan
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

package ai.kumar.tools;

/**
 * Created by chetankaushik on 04/08/17.
 */
import java.io.IOException;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

/**
 * Handler that adds the multipart config to the request that passes through if
 * it is a multipart request.
 *
 * <p>
 * Jetty will only clean up the temp files generated by
 * {@link MultiPartInputStreamParser} in a servlet event callback when the
 * request is about to die but won't invoke it for a non-servlet (webapp)
 * handled request.
 *
 * <p>
 * MultipartConfigInjectionHandler ensures that the parts are deleted after the
 * {@link #handle(String, Request, HttpServletRequest, HttpServletResponse)}
 * method is called.
 *
 * <p>
 * Ensure that no other handlers sit above this handler which may wish to do
 * something with the multipart parts, as the saved parts will be deleted on the return
 * from
 * {@link #handle(String, Request, HttpServletRequest, HttpServletResponse)}.
 */
public class MultipartConfigInjectionHandler extends HandlerWrapper {
    public static final String MULTIPART_FORMDATA_TYPE = "multipart/form-data";

    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(
            System.getProperty("java.io.tmpdir"));

    public static boolean isMultipartRequest(ServletRequest request) {
        return request.getContentType() != null
                && request.getContentType().startsWith(MULTIPART_FORMDATA_TYPE);
    }

    /**
     * If you want to have multipart support in your handler, call this method each time
     * your doHandle method is called (prior to calling getParameter).
     *
     * Servlet 3.0 include support for Multipart data with its
     * {@link HttpServletRequest#getPart(String)} & {@link HttpServletRequest#getParts()}
     * methods, but the spec says that before you can use getPart, you must have specified a
     * {@link MultipartConfigElement} for the Servlet.
     *
     * <p>
     * This is normally done through the use of the MultipartConfig annotation of the
     * servlet in question, however these annotations will not work when specified on
     * Handlers.
     *
     * <p>
     * The workaround for enabling Multipart support in handlers is to define the
     * MultipartConfig attribute for the request which in turn will be read out in the
     * getPart method.
     *
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=395000#c0">Jetty Bug
     *      tracker - Jetty annotation scanning problem (servlet workaround) </a>
     * @see <a href="http://dev.eclipse.org/mhonarc/lists/jetty-users/msg03294.html">Jetty
     *      users mailing list post.</a>
     */
    public static void enableMultipartSupport(HttpServletRequest request) {
        request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        boolean multipartRequest = HttpMethod.POST.is(request.getMethod())
                && isMultipartRequest(request);
        if (multipartRequest) {
            enableMultipartSupport(request);
        }

        try {
            super.handle(target, baseRequest, request, response);
        } finally {
            if (multipartRequest) {
                MultiPartInputStreamParser multipartInputStream = (MultiPartInputStreamParser) request
                        .getAttribute(Request.__MULTIPART_INPUT_STREAM);
                if (multipartInputStream != null) {
                    try {
                        // a multipart request to a servlet will have the parts cleaned up correctly, but
                        // the repeated call to deleteParts() here will safely do nothing.
                        multipartInputStream.deleteParts();
                    } catch (MultiException e) {
//            LOG.error("Error while deleting multipart request parts", e);
                    }
                }
            }
        }
    }
}

