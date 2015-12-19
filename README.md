# android-identity-manager

From Jiewen Tan https://github.com/alanwaketan/android-identity-manager

Updated Zhehao <zhehao@cs.ucla.edu> 

Dec 18, 2015

### How to use:

* The app uses a default certificate issueing website at: http://memoria.ndn.ucla.edu:5001, whose interface's documented at: https://github.com/zhehaowang/openmhealth-cert#webmobile-app-interface
* Steps to create an OpenmHealth identity:
  * Launch application, choose "Create an ID" 
  * Accept the terms of service
  * Put your email address and give the identity a name (Just a name for user rememberance, has nothing to do with NDN name) 
  * Submit token request
  * Check email for the subject "[NDN Open mHealth Certification] request confirmation", open the link with the option "open link in ID manager"
  * Put your name and submit (Would remove "full name" if not needed? For now it's the "subject name" field in the cert, but could use the user given name from previous step instead)
  * Check email for the subject "[NDN Open mHealth Certification] certificate issued", open the link with the option "open link in ID manager", and click Ok after the successful installation message shows up
* To see what identities are created
  * Launch application, choose "Trace all identities"
* To test with identity selection in the ndnfit application
  * Launch ndnfit, click Ok and the user would be redirected to the same view as "Trace all identities", and is able to select an identity

### TODOs:
* Conforming with Dustin UI
  * Main activity should point to circles with avatars and names
  * Minizing what the user has to do in the first round of interaction with Email; Confirmation and installation steps should both point to the view with steps
  * Current implementation has a "device identity" which is signed by "user identity", and is used to sign application data; not sure if we want this for the initial implementation; for now, just treat it as similar with the "user identity"
  * Check on the location of "KEY" component in Open mHealth cert names?
  * UI element improvements

### Development:

* Open in Android Studio, SDK 22, build tools 22.0.1; sync Gradle
* Clone 'volley': git clone https://android.googlesource.com/platform/frameworks/volley (currently not in submodule)