package android.print;

/*
 * Created on 11/15/17.
 * Written by Islam Salah with assistance from members of Blink22.com
 */

 import android.content.Context;
 import android.os.Build;
 import android.os.Handler;
 import android.os.ParcelFileDescriptor;
 import android.print.PageRange;
 import android.print.PrintAttributes;
 import android.print.PrintDocumentAdapter;
 import android.util.Log;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;

 import dnd.jon.spellbook.SpellbookUtils;

/**
 * Converts HTML to PDF.
 * <p>
 * Can convert only one task at a time, any requests to do more conversions before
 * ending the current task are ignored.
 */
public class PdfConverter implements Runnable {

    private static final String TAG = "PdfConverter";

    private Context mContext;
    private String mHtmlString;
    private File mPdfFile;
    private PrintAttributes mPdfPrintAttrs;
    private boolean mIsCurrentlyConverting;
    private WebView mWebView;
    private OutputStream mOutputStream;
    private final Context context;

    public PdfConverter(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        mWebView = new WebView(mContext);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                    throw new RuntimeException("call requires API level 19");
                else {
                    PrintDocumentAdapter documentAdapter = mWebView.createPrintDocumentAdapter("document");
                    documentAdapter.onLayout(null, getPdfPrintAttrs(), null, new PrintDocumentAdapter.LayoutResultCallback() {
                    }, null);
                    documentAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, getOutputFileDescriptor(), null, new PrintDocumentAdapter.WriteResultCallback() {
                        @Override
                        public void onWriteFinished(PageRange[] pages) {
                            try {
                                final InputStream inStream = new FileInputStream(mPdfFile);
                                SpellbookUtils.copy(inStream, mOutputStream);
                                mOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            destroy();
                        }
                    });
                }
            }
        });
        mWebView.loadData(mHtmlString, "text/HTML", "UTF-8");
    }

    public PrintAttributes getPdfPrintAttrs() {
        return mPdfPrintAttrs != null ? mPdfPrintAttrs : getDefaultPrintAttrs();
    }

    public void setPdfPrintAttrs(PrintAttributes printAttrs) {
        this.mPdfPrintAttrs = printAttrs;
    }

    public void convert(Context context, String htmlString, OutputStream stream) {
        if (context == null)
            throw new IllegalArgumentException("context can't be null");
        if (htmlString == null)
            throw new IllegalArgumentException("htmlString can't be null");

        if (mIsCurrentlyConverting)
            return;

        File tempFile;
        try {
            tempFile = File.createTempFile("tmp", "pdf", context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mContext = context;
        mHtmlString = htmlString;
        mPdfFile = tempFile;
        mOutputStream = stream;
        mIsCurrentlyConverting = true;
        runOnUiThread(this);
    }

    private ParcelFileDescriptor getOutputFileDescriptor() {
        try {
            mPdfFile.createNewFile();
            return ParcelFileDescriptor.open(mPdfFile, ParcelFileDescriptor.MODE_TRUNCATE | ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (Exception e) {
            Log.d(TAG, "Failed to open ParcelFileDescriptor", e);
        }
        return null;
    }

    private PrintAttributes getDefaultPrintAttrs() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return null;

        return new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_GOVT_LETTER)
                .setResolution(new PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

    }

    private void runOnUiThread(Runnable runnable) {
        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(runnable);
    }

    private void destroy() {
        mContext = null;
        mHtmlString = null;
        mPdfFile = null;
        mPdfPrintAttrs = null;
        mIsCurrentlyConverting = false;
        mWebView = null;
    }
}
