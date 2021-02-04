import 'dart:collection';

import 'package:audiole/audiole.dart';
import 'package:audiole_example/AudioPage.dart';
import 'package:audiole_example/anime/fade_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

import 'helperclass.dart';

class FileManager extends StatefulWidget {
  @override
  _FileManagerState createState() => _FileManagerState();
}

class _FileManagerState extends State<FileManager> {
  List<dynamic> dirs;
  List<dynamic> media;
  List<FolderInfo> Folderinfo;
  String path,currentFolder,parentDirectory;

  @override
  void initState() {
    super.initState();
    dirs=[""];
    media=[""];
    Folderinfo=List<FolderInfo>();
    path="";
    parentDirectory="";
    getFolderInfo(path);
  }

  void getFolderInfo(filepath) async {
    print(filepath);
    if(!filepath.startsWith(parentDirectory))
      filepath=parentDirectory;
    print(filepath);
    await Audiole.fileManager(filepath).then((value) {
      Map<String,dynamic> map=Map.from(value);
      dirs=map["dirs"];
      media=map["media"];
      Folderinfo.clear();
      int i=0;
      for(;i<dirs.length;i++){
        Folderinfo.add(FolderInfo(dirs[i],true));
      }
      for(;i<dirs.length+media.length;i++){
        Folderinfo.add(FolderInfo(media[i-dirs.length],false));
      }
      setState(() {
        path=map["path"];
        if(parentDirectory=="")
          parentDirectory=path;
      });

    });
  }

  Widget upFolder(){
   return GestureDetector(
      child:  Container(
          padding: EdgeInsets.all(20),
          child: Row(
            children: [
            Icon(Icons.folder),
              Padding(
                child:  Text("Up a Folder",style: Theme.of(context).textTheme.bodyText1,),
                padding: EdgeInsets.only(left: 30),
              )
            ],
            mainAxisAlignment: MainAxisAlignment.start,
          )),
      onTap: (){
        getFolderInfo(path.substring(0,path.lastIndexOf('/')));
        // getFolderInfo(path.substring(0,currentFolder.length+1));
      },
    );
  }


  @override
  Widget build(BuildContext context) {
   return
     WillPopScope(onWillPop: () async {
       if (path!=parentDirectory) {
         getFolderInfo(path.substring(0,path.lastIndexOf('/')));
       return false;
     } else {
       return true;
     }
     },
     child:
     Scaffold(
         body:
         Column(
           mainAxisAlignment: MainAxisAlignment.center,
           crossAxisAlignment: CrossAxisAlignment.start,
           children: [
             SizedBox(height: MediaQuery.of(context).padding.top,),
             Text("File Manager",style: Theme.of(context).textTheme.headline3,),
             if(path!=parentDirectory)
               Folderinfo.isEmpty?Expanded(
                   flex: 10,
                   child: Center(child: upFolder(),)
               ):
               Container(
                 child: upFolder(),
               ),
             Divider(thickness: 3,
               height: 10,
               color: Colors.black12,),
             Expanded(
               flex: 1,
               child: ListView.builder(
                 padding: EdgeInsets.all(0),
                 shrinkWrap: true,
                 itemCount: Folderinfo.length,
                 itemBuilder: (BuildContext context, int index) {
                   return
                     Column(
                       children: [
                         Divider(thickness: 3,
                           height: 10,
                           color: Colors.black12,),
                         GestureDetector(
                             onTap: (){
                               if(Folderinfo[index].isDir)
                                 getFolderInfo(path+'/'+Folderinfo[index].name);
                               else
                                 Navigator.push(context,FadeRoute(page: AudioPage(
                                   path,Folderinfo,index
                                 )) );
                             },
                             child:
                             Row(
                               children: [
                                 Expanded(
                                   flex:1,
                                   child:Container(
                                       color: Colors.white10,
                                       // height: 10,
                                       padding: EdgeInsets.all(20),
                                       child: Row(
                                         children: [
                                          Padding(child:  Folderinfo[index].isDir?Icon(Icons.folder):Icon(Icons.music_note_rounded),
                                              padding: EdgeInsets.only(right: 20),),
                                          Flexible(
                                            child:  Text(Folderinfo[index].name,style: Theme.of(context).textTheme.bodyText1,),
                                          )
                                         ],
                                         mainAxisAlignment: MainAxisAlignment.start,
                                       )),
                                 )
                               ],
                             )

                         )

                       ],
                     );
                 },),
             )
           ],

         )
     ),);
  }
  
}