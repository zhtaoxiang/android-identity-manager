// running in node: export NODE_PATH=$NODE_PATH:~/projects/ndn/ndn-clones/

// data name example
// /org/openmhealth/BwInbugZnm7dS0zjZKa3Q0PlFooURUv8A6MOnOMz4wyXqTxiIrYNn7BWjAeF/data/fitness/physical_activity/time_location/%FC%00%00%01R%5C%08N%9B
// catalog name example
// /org/openmhealth/BwInbugZnm7dS0zjZKa3Q0PlFooURUv8A6MOnOMz4wyXqTxiIrYNn7BWjAeF/data/fitness/physical_activity/time_location/catalog/%FC%00%00%01R%5C%0Br%00/%FD%01

var Face = require('ndn-js').Face;
var Name = require('ndn-js').Name;
var UnixTransport = require('ndn-js').UnixTransport;
var Exclude = require('ndn-js').Exclude;

var Config = {
	hostName: "memoria.ndn.ucla.edu",
  wsPort: 9696,
  defaultUsername: "BwInbugZnm7dS0zjZKa3Q0PlFooURUv8A6MOnOMz4wyXqTxiIrYNn7BWjAeF",
  defaultPrefix: "/org/openmhealth/",
  catalogPrefix: "/data/fitness/physical_activity/time_location/catalog/",
  dataPrefix: "/data/fitness/physical_activity/time_location/",
  defaultInterestLifetime: 1000,
  // not a reliable way for determining if probe has finished
  catalogTimeoutThreshold: 3
}

var face = new Face({host: Config.hostName, port: Config.wsPort});
var catalogTimeoutCnt = 0;

var userCatalogs = [];
var catalogProbeFinished = false;

var catalogProbeFinishedCallback = null;

var onCatalogData = function(interest, data) {
  var catalogTimestamp = data.getName().get(-2);
  var exclude = new Exclude();
  exclude.appendAny();
  // this looks for the next catalog of this user
  exclude.appendComponent(catalogTimestamp);

  var nextCatalogInterest = new Interest(interest);
  nextCatalogInterest.setExclude(exclude);
  face.expressInterest(nextCatalogInterest, onCatalogData, onCatalogTimeout);

  // this looks for the latest version of this catalog; note: this is not a reliable way to get the latest version
  var catalogVersion = data.getName().get(-1);
  var nextVersionInterest = new Interest(interest);
  nextVersionInterest.setName(data.getName().getPrefix(-1));
  // to exclude the cached received version;
  var versionExclude = new Exclude();
  versionExclude.appendAny();
  versionExclude.appendComponent(catalogVersion);
  nextVersionInterest.setExclude(versionExclude);
  
  face.expressInterest(nextVersionInterest, onCatalogVersionData, onCatalogVersionTimeout);
  catalogTimeoutCnt = 0;

  onCatalogVersionData(interest, data);
};

var onCatalogVersionData = function(interest, data) {
  console.log("Got versioned catalog: " + data.getName().toUri());

  var catalogVersion = data.getName().get(-1);
  var catalogTimestamp = data.getName().get(-2);

  var dataContent = JSON.parse(data.getContent().buf().toString('binary'));
  var username = interest.getName().get(2).toEscapedString();
  if (username in userCatalogs) {
    if (catalogTimestamp.toTimestamp() in userCatalogs[username]) {
      if (userCatalogs[username][catalogTimestamp.toTimestamp()].last_version < catalogVersion.toVersion()) {
        userCatalogs[username][catalogTimestamp.toTimestamp()] = {"last_version": catalogVersion.toVersion(), "content": dataContent};
      } else {
        console.log("Received duplicate or previous version.");
      }
    } else {
      userCatalogs[username][catalogTimestamp.toTimestamp()] = {"last_version": catalogVersion.toVersion(), "content": dataContent};
    }
  } else {
    userCatalogs[username] = [];
    userCatalogs[username][catalogTimestamp.toTimestamp()] = {"last_version": catalogVersion.toVersion(), "content": dataContent};
  }
}

var onCatalogVersionTimeout = function(interest) {
  console.log("Catalog version times out.");
}

var onCatalogTimeout = function(interest) {
  console.log("Time out for catalog interest " + interest.getName().toUri());
  catalogTimeoutCnt += 1;
  if (catalogTimeoutCnt < Config.catalogTimeoutThreshold) {
    face.expressInterest(interest, onCatalogData, onCatalogTimeout);
  } else {
    console.log("No longer looking for more catalogs.");
    catalogProbeFinished = true;
    if (catalogProbeFinishedCallback != null) {
      catalogProbeFinishedCallback(userCatalogs);
    } else {
      console.log("Catalog probe finished, callback unspecified");
    }
  }
};

// given a userPrefix, populates userCatalogs[username] with all of its catalogs
function getCatalogs(userPrefix) {
  if (userPrefix == undefined) {
    userPrefix = Config.defaultPrefix + Config.defaultUsername;
  }
  var name = new Name(userPrefix + Config.catalogPrefix);
  var interest = new Interest(name);
  interest.setInterestLifetimeMilliseconds(Config.defaultInterestLifetime);
  // start from leftmost child
  interest.setChildSelector(0);
  interest.setMustBeFresh(true);

  console.log("Express name " + name.toUri());
  face.expressInterest(interest, onCatalogData, onCatalogTimeout);

  document.getElementById("content").innerHTML += "Fetching fitness data under prefix: " + userPrefix + "<br>";
;
};

function getData(catalogs) {
  if (!catalogProbeFinished) {
    console.log("Catalog probe still in progress; may fetch older versioned data.");
  }
  for (username in catalogs) {
    var name = new Name(Config.defaultPrefix + username + Config.dataPrefix);
    for (catalog in catalogs[username]) {
      for (dataItem in catalogs[username][catalog].content) {
        var interest = new Interest(new Name(name).appendTimestamp(catalogs[username][catalog].content[dataItem]));
        interest.setInterestLifetimeMilliseconds(Config.defaultInterestLifetime);
        face.expressInterest(interest, onAppData, onAppDataTimeout);
      }
    }
  }
}

function formatTime(unixTimestamp) {
  var a = new Date(unixTimestamp);
  var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  var year = a.getFullYear();
  var month = months[a.getMonth()];
  var date = a.getDate();
  var hour = a.getHours();
  var min = a.getMinutes();
  var sec = a.getSeconds();
  var time = date + ' ' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec ;
  return time;
}

var onAppData = function (interest, data) {
  console.log("Got fitness data: " + data.getName().toUri());
  var content = JSON.parse(data.getContent().buf().toString('binary'));
  console.log("Fitness payload: " + JSON.stringify(content));
  console.log("Data keyLocator keyName: " + data.getSignature().getKeyLocator().getKeyName().toUri());
  for (var i = 0; i < content.length; i++) {
    document.getElementById("content").innerHTML += formatTime(content[i].timeStamp) + "  " + JSON.stringify(content[i]) + "<br>";
  }
}

var onAppDataTimeout = function (interest) {
  console.log("App interest times out: " + interest.getName().toUri());
}