package ru.qweert.martha;

import java.io.File;

public interface XmlTransformer {

    File xmlToHtml(File input, File schema);
}
