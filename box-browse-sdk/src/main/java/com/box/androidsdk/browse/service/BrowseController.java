package com.box.androidsdk.browse.service;

import android.content.Context;

import com.box.androidsdk.browse.uidata.ThumbnailManager;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.content.requests.BoxRequestsSearch;
import com.box.androidsdk.content.requests.BoxResponse;

import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

/***
 * Controller interface for the Box Browse SDK. This defines all of the requests that will be used by the SDK.
 */
public interface BrowseController {

    /***
     * Retrieves a folder with all its items. The response will be returned through the provided listener
     *
     * @param folderId
     */
    BoxRequestsFolder.GetFolderWithAllItems getFolderWithAllItems(String folderId);

    /***
     * Retrieves search results for the given query
     *
     * @param query
     * @return
     */
    BoxRequestsSearch.Search getSearchRequest(String query);

    /***
     * Retrieves a thumbnail for a file. The response will be returned through the provided listener
     *
     * @param fileId
     * @param downloadFile
     * @return
     */
    BoxRequestsFile.DownloadThumbnail getThumbnailRequest(String fileId, File downloadFile);


    /***
     * Executes the request using the appropriate executor
     *
     * @param request
     */
    void execute(BoxRequest request);

    /***
     * Sets the default compeltion listener that will be used after the completion of a BoxRequest
     *
     * @param listener
     * @return
     */
    BrowseController setCompletedListener(BoxFutureTask.OnCompletedListener listener);

    /**
     * Error handler for whenever an error occurs from a request
     *
     * @param context
     * @param response response returned from the server that contains the request, result, and exception
     */
    void onError(Context context, BoxResponse response);

    File getThumbnailCacheDir();

    ThumbnailManager getThumbnailManager();


    /**
     * Returns the executor used for thumbnail api requests
     * @return
     */

    ThreadPoolExecutor getThumbnailExecutor();

    /**
     * Use for any custom logging
     * @param msg
     * @param tag
     * @param t
     */

    void Log(String tag, String msg, Throwable t);
}