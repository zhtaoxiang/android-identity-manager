# android-identity-manager

Zhehao <zhehao@cs.ucla.edu> (Original work by Jiewen Tan, https://github.com/alanwaketan/android-identity-manager)

Jan 9, 2015 - v0.2

Screen recording on Android 4.4.4 (Kitkat): https://www.youtube.com/watch?v=uFrymOzjJt8

### What it does:

* Request an Open mHealth id (similar with NDN cert)
  * Generate user and device identity
  * Request a user identity to be signed by Open mHealth root
  * Sign a device identity with user identity
* Authorize NDNFit application
  * Sign an application identity with user identity
  * Display authorized applications

### How to use:

* Steps to create an OpenmHealth identity:
  * Launch application, tap one of the plus signs above each "Create new ID"
  * Accept the terms of service
  * Put your email address and give the identity a name (this name is kept locally, and has nothing to do with the ID's NDN name), click next
  * Choose a profile picture (optional), click submit
  * Open email application, and check email for the subject "[NDN Open mHealth Certification] request confirmation", open the link with the option "open link in ID manager"
  * Wait for the ID manager to confirm the request, then open email application again, and check email for the subject "[NDN Open mHealth Certification] certificate issued", open the link with the option "open link in ID manager", and wait till the "cert installed" confirmation shows up.
* To see what applications are authorized with a certain identity, click the identity image in main activity.
* (Coming soon) Upon launching NDNFit capture application for the first time, the user will be prompted to choose an identity with ID manager. Current NDNFit apk that behaves like this can be built from: https://github.com/zhehaowang/ndnfit/tree/with-id-manager.

* Common problems:
  * Getting the apk: https://github.com/zhehaowang/android-identity-manager/releases/download/v0.2/identity-manager-0.2.apk; Please check "allow from unverified sources" if accessed from an Android device
  * Stuck on "submit token", or "confirm request": Check phone's internet connection; If working, the connection may have blocked outgoing traffic to port 5001 on memoria.ndn.ucla.edu (which runs the Open mHealth cert service)

### TODOs:

* Testing on post-lollipop Android devices (5.0+)
* Config verification policy; Integration tests with capture app, DSU and basic DVU
* Exception handling
  * Installing the same ID twice
  * Unexpected interaction between apps, and between cert website
  * No network connection
  * ...
* UI improvements
  * Weird looking Floating Action Buttons on Main activity on pre-lollipop devices
  * Profile images, and their selection layout; selecting images from gallery
  * UI for authorized applications page
* Conforming with user id, device id, app id design
  * Current implementation has a "device identity" which is signed by "user id", and "user id" used to sign "app id", which signs the data
* Miscellaneous
  * Check on the location of "KEY" component in Open mHealth cert names
  * ...

### Notes:

* The app uses a default certificate issueing website at: http://memoria.ndn.ucla.edu:5001, whose interface's documented at: https://github.com/zhehaowang/openmhealth-cert#webmobile-app-interface. The site's based on https://github.com/named-data/ndncert
* To see the list of issued identities, go to http://memoria.ndn.ucla.edu:5001/cert/list/html
* The NDNFit capture application will be released soon; 
* The two interactions with email application: first one gets the assigned namespace, and verifies that the user owns the email address; second one gets the signed certificate, if it's approved. (Currently split in two, could merge into one?)

### Development:

* Open in Android Studio, SDK 22, build tools 22.0.1; sync Gradle
* Clone 'volley': git clone https://android.googlesource.com/platform/frameworks/volley (currently not in submodule)
