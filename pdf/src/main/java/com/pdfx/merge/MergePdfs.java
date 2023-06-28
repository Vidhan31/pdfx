package com.pdfx.merge;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

public class MergePdfs {

    private final List<File> originalFileList;
    private final List<Integer> order;
    private final String filename;

    public MergePdfs(MergerFiles mergerFiles) {

        this.originalFileList = mergerFiles.fileList();
        this.order = mergerFiles.order();
        this.filename = mergerFiles.fileName();
    }

    public boolean merge(boolean mergeAll) throws IOException {

        var merger = new PDFMergerUtility();

        String destinationDirectory = originalFileList.get(0).getParent();
        var file = new File(destinationDirectory, filename);
        merger.setDestinationFileName(file.getAbsolutePath());

        if (!mergeAll) {

            for (int i = 0; i < order.size(); i++)
                merger.addSource(originalFileList.get(order.get(i) - 1));

        } else {

            for (File value : originalFileList)
                merger.addSource(value);
        }

        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return true;
    }
}
