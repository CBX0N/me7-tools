package com.prj.tuning.mappack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.prj.tuning.mappack.map.PMap;
import com.prj.tuning.mappack.util.BinaryUtil;

public class Project {
  private Header header;
  private Collection<PMap> maps;
  private Collection<Folder> folders;
  private URL projectUrl;
  private ByteBuffer projectData;
  private boolean parsed;

  public Project(URL projectUrl) {
    this.projectUrl = projectUrl;
    this.parsed = false;
  }

  public Project parse() throws IOException {
    if (!parsed) {
      BufferedInputStream is = null;
      try {
        is = new BufferedInputStream(projectUrl.openStream());
        // Allocate off-heap
        projectData = ByteBuffer.allocateDirect(is.available());
        projectData.order(ByteOrder.LITTLE_ENDIAN);
        BinaryUtil.transferToBuf(projectData, is);
        header = Header.fromBuffer(projectData);

        maps = new HashSet<PMap>();

        for (int i = 0; i < header.getMapCount(); i++) {
          maps.add(PMap.fromBuffer(projectData));
        }

        BinaryUtil.skip(projectData, 12);
        
        int folderCount = projectData.getInt();
        folders = new HashSet<Folder>();
        
        for (int i = 0; i < folderCount; i++) {
          folders.add(Folder.fromBuffer(projectData));
        }
      }
      finally {
        if (is != null)
          is.close();
      }
    }
    return this;
  }

  public Header getHeader() {
    return header;
  }

  public Collection<PMap> getMaps() {
    return Collections.unmodifiableCollection(maps);
  }

  public Collection<Folder> getFolders() {
    return Collections.unmodifiableCollection(folders);
  }
  
}