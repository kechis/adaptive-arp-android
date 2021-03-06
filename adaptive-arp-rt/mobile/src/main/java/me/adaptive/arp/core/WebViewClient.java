package me.adaptive.arp.core;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.apache.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import me.adaptive.arp.R;
import me.adaptive.arp.api.APIRequest;
import me.adaptive.arp.api.APIResponse;
import me.adaptive.arp.api.AppRegistryBridge;
import me.adaptive.arp.api.AppResourceData;
import me.adaptive.arp.api.ILogging;
import me.adaptive.arp.api.ILoggingLogLevel;
import me.adaptive.arp.common.Utils;
import me.adaptive.arp.common.core.AppResourceManager;

/**
 * Http Interceptor for handling requests inside an Adaptive Runtime Application. More information
 * on: https://github.com/AdaptiveMe/adaptive-arp-api/wiki/ARP-HTTP-Interceptor
 */
public class WebViewClient extends android.webkit.WebViewClient {

    // Logger
    private static final String LOG_TAG = "WebViewClient";
    private ILogging logger;

    // Context
    private Context context;

    /**
     * Default Constructor.
     */
    public WebViewClient() {
        logger = AppRegistryBridge.getInstance().getLoggingBridge();
        context = (Context) AppRegistryBridge.getInstance().getPlatformContext().getContext();
    }

    /**
     * Notify the host application of a resource request and allow the application to return the
     * data. If the return value is null, the WebView will continue to load the resource as usual.
     * Otherwise, the return response and data will be used. NOTE: This method is called on a thread
     * other than the UI thread so clients should exercise caution when accessing private data or
     * the view system.
     *
     * @param view    The WebView that is requesting the resource.
     * @param request Object containing the details of the request.
     * @return A WebResourceResponse containing the response information or null if the WebView should load the resource itself.
     */
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

        WebResourceResponse response = null;
        String method = request.getMethod();
        String url = request.getUrl().toString();

        try {
            if (!(url == null || url.isEmpty() || method == null || method.isEmpty())) {

                if (url.startsWith(context.getString(R.string.arp_url)) && method.equals("GET")) {

                    // FILE MANAGEMENT (via Adaptive Packer)

                    logger.log(ILoggingLogLevel.Debug, LOG_TAG, "Intercepting File request: " + request.getUrl().toString());

                    AppResourceData resource = AppResourceManager.getInstance().retrieveWebResource(url);

                    // Prepare the response
                    response = new WebResourceResponse(resource.getRawType(),
                            "UTF-8", 200, "OK", request.getRequestHeaders(),
                            new ByteArrayInputStream(resource.getData()));
                    return response;

                } else if (url.startsWith(context.getString(R.string.arp_url)) && method.equals("POST")) {

                    // ADAPTIVE NATIVE CALLS

                    APIRequest apiRequest = AppRegistryBridge.getJSONInstance().create().fromJson(request.getRequestHeaders().get("Content-Body"), APIRequest.class);
                    logger.log(ILoggingLogLevel.Debug, LOG_TAG, "Intercepting ARP request: " + apiRequest);

                    if (!apiRequest.getApiVersion().equals(AppRegistryBridge.getInstance().getAPIVersion())) {
                        logger.log(ILoggingLogLevel.Warn, LOG_TAG, "The API version of the Typescript API is not the same as the Platform API version");
                    }

                    // Call the service and return the data
                    APIResponse apiResponse = ServiceHandler.getInstance().handleServiceUrl(apiRequest);

                    // Prepare the response
                    try {
                        response = new WebResourceResponse("application/javascript; charset=utf-8",
                                "UTF-8", apiResponse.getStatusCode(), apiResponse.getStatusMessage(),
                                request.getRequestHeaders(), new ByteArrayInputStream(AppRegistryBridge.getJSONInstance().create().toJson(apiResponse).getBytes("utf-8")));
                    } catch (UnsupportedEncodingException e) {
                        logger.log(ILoggingLogLevel.Error, LOG_TAG, "shouldInterceptRequest Error:" + e.getLocalizedMessage());
                        return nonPermissionResponse(request);
                    }
                    return response;

                } else {
                    if (Utils.validateUrl(url)) return null;
                    else return nonPermissionResponse(request);
                }
            } else {
                logger.log(ILoggingLogLevel.Error, LOG_TAG, "The method or the url is received is empty");
                return nonPermissionResponse(request);
            }
        } catch (Exception e) {

            logger.log(ILoggingLogLevel.Error, LOG_TAG, "shouldInterceptRequest Error:" + e.getLocalizedMessage());
            return nonPermissionResponse(request);
        }
    }


    /**
     * Notify the host application that a page has finished loading. This method
     * is called only for main frame. When onPageFinished() is called, the
     * rendering picture may not be updated yet. To get the notification for the
     * new Picture, use {@link android.webkit.WebView.PictureListener#onNewPicture}.
     *
     * @param view The WebView that is initiating the callback.
     * @param url  The url of the page.
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        // TODO: In jQuery Apps this method is calling twice
        if (url.equals(context.getString(R.string.arp_url) + context.getString(R.string.arp_page))) {
            AppRegistryBridge.getInstance().getRuntimeBridge().dismissSplashScreen();
        }
    }

    /**
     * Create a non-permission response for the untrusted requests to the Adaptive Core
     *
     * @param request Object containing the details of the request.
     * @return Response for non-permission requests
     */
    private WebResourceResponse nonPermissionResponse(WebResourceRequest request) {

        WebResourceResponse response = null;
        try {
            response = new WebResourceResponse("application/javascript; charset=utf-8",
                    "UTF-8", HttpStatus.SC_FORBIDDEN, "The service you're trying to call is not registered in the io-services config file.",
                    request.getRequestHeaders(), new ByteArrayInputStream(AppRegistryBridge.getJSONInstance().create().toJson("The service you're trying to call is not registered in the io-services config file.").getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            logger.log(ILoggingLogLevel.Error, LOG_TAG, "nonPermissionResponse Error:" + e.getLocalizedMessage());
        }
        return response;
    }


}