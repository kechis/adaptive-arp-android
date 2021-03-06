/**
 --| ADAPTIVE RUNTIME PLATFORM |----------------------------------------------------------------------------------------

 (C) Copyright 2013-2015 Carlos Lozano Diez t/a Adaptive.me <http://adaptive.me>.

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 . Unless required by appli-
 -cable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the  License  for the specific language governing
 permissions and limitations under the License.

 Original author:

 * Carlos Lozano Diez
 <http://github.com/carloslozano>
 <http://twitter.com/adaptivecoder>
 <mailto:carlos@adaptive.me>

 Contributors:

 * Ferran Vila Conesa
 <http://github.com/fnva>
 <http://twitter.com/ferran_vila>
 <mailto:ferran.vila.conesa@gmail.com>

 * See source code files for contributors.

 Release:

 * @version v2.0.3

-------------------------------------------| aut inveniam viam aut faciam |--------------------------------------------
 */

package me.adaptive.arp.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import me.adaptive.arp.BrowserActivity;
import me.adaptive.arp.api.AppRegistryBridge;
import me.adaptive.arp.api.BaseUIDelegate;
import me.adaptive.arp.api.IBrowser;
import me.adaptive.arp.api.ILogging;
import me.adaptive.arp.api.ILoggingLogLevel;

/**
 * Interface for Managing the browser operations
 * Auto-generated implementation of IBrowser specification.
 */
public class BrowserDelegate extends BaseUIDelegate implements IBrowser {

    // logger
    private static final String LOG_TAG = "BrowserDelegate";
    private ILogging logger;

    // Context
    private Context context;

    /**
     * Default Constructor.
     */
    public BrowserDelegate() {
        super();
        logger = AppRegistryBridge.getInstance().getLoggingBridge();
        context = (Context) AppRegistryBridge.getInstance().getPlatformContext().getContext();
    }

    /**
     * Method for opening a URL like a link in the native default browser
     *
     * @param url Url to open
     * @return The result of the operation
     * @since ARP1.0
     */
    public boolean openExtenalBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception ex) {
            logger.log(ILoggingLogLevel.Error, LOG_TAG, "openExtenalBrowser Error: "+ ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Method for opening a browser embedded into the application
     *
     * @param url            Url to open
     * @param title          Title of the Navigation bar
     * @return The result of the operation
     * @since ARP1.0
     */
    private boolean openInternalBrowser(String url, String title,boolean modal) {

        try {
            Intent intent = new Intent(context, BrowserActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("title", title);
            intent.putExtra("modal", modal);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception ex) {
            logger.log(ILoggingLogLevel.Error, LOG_TAG, "openInternalBrowser Error: " + ex.getLocalizedMessage());
            return false;
        }

    }


    /**
     * Method for opening a browser embedded into the application
     *
     * @param url            Url to open
     * @param title          Title of the Navigation bar
     * @param backButtonText Title of the Back button bar
     * @return The result of the operation
     * @since ARP1.0
     */
    public boolean openInternalBrowser(String url, String title, String backButtonText) {
        return openInternalBrowser(url,title,false);
    }

    /**
     * Method for opening a browser embedded into the application in a modal window
     *
     * @param url            Url to open
     * @param title          Title of the Navigation bar
     * @param backButtonText Title of the Back button bar
     * @return The result of the operation
     * @since ARP1.0
     */
    public boolean openInternalBrowserModal(String url, String title, String backButtonText) {
        return openInternalBrowser(url,title,true);
    }

}
/**
 ------------------------------------| Engineered with ♥ in Barcelona, Catalonia |--------------------------------------
 */
