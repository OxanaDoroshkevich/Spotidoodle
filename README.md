# SPOTIDOODLE

## A project using the Spotify web API and the Spotify android SDK
The idea was to create an application with the use of the android Spotify SDK or the Web API which allows to sort
your with Spotify created Playlist more specifically. At the moment, right now Spotify provides only a sort
algorithm that allows to sort alphabetically the titles or the interpreter names, after the date a song was added
and the albums. The idea was to provide another sort algorithm that allows you to sort the songs after the BPM,
the danceability, the party suitability and so on. Further the application should keep the functionality simple.
Given that this is “just” an addon for the real Spotify application you can’t search for songs or create new playlists
(things you can do in the Spotify application). The second idea was to provide a possibility to “rate” the songs of a
playlist. The aim is to make a one button click solution for the user to store a song in his before chosen playlist, a
one button click to delete the song of the actual playlist, to skip to the drop of a song or to skip the actual song.

### Developing Vincent Weiss, Oxana Doroshkevich, Marco Himmelstein, Christoph Dörr, Frederick Wurfer

### Tools

1. Sporify Web API
2. Spotify Android SDK

### Prerequisites

You need Android Studio and the android 23.0.1 SDK or higher

### Installing

fork this reposototy to your own account and switch to the forked one
now in your new "own" forked reposotory click on the green "clone or download" button and download the zip file
1. open git bash
2. in git bash: switch to the directory you have chosen for the downloaded and hopefully now unzipped project file
3. in git bash: git init
4. git remote add orinin https://github.com/YOURREPOSOTORYNAMEHERE/Spotidoodle.git
5. git remote add SOMENAMEOFAREPOHERE https://github.com/VincWeiss/Spotidoodle.git
6. check your reposotory status with: git remote -v
this should allow you now to push to your own master and pull from the master master ;)

if you want to commit some changes, follow the following instruction:

1. git status
2. git add . (for all files that were changed) OR git add filename (for specific file)
3. git commit -m "SOME MESSAGE HERE"
4. git push origin master
I would recommend to do the pull requests to merge the local code to the master master branch. so that everything is alwas save from merging error!

Good to know. If you have some problems with git and you can't pull or push or even checkout the master again or something and you're just fucking stuck...

git fetch --all
git reset --hard origin/master

to get the version of your accounts master branch status.

## Coding style
    
``` Spotify android SDK authentication
protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;
                case ERROR:
                    Log.d("Auth error: ", response.getError());
                    break;
                default:
                    Log.d("Auth error: ", response.getType().toString());
            }
        }
    }
```

### Built With

http://kaaes.github.io/spotify-web-api-android/javadoc/
https://github.com/kaaes/spotify-web-api-android
http://www.programcreek.com/java-api-examples/index.php?api=kaaes.spotify.webapi.android.models.Playlist
http://www.programcreek.com/java-api-examples/index.php?api=kaaes.spotify.webapi.android.SpotifyService

### Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

### Versioning

Version 5.0

## Authors

* **Vincent Weiss** - *Initial work* - [Vincent Weiss](https://github.com/VincWeiss)
* **Oxana Doroshkevich** - *Design work* - [Oxana Doroshkevich](https://github.com/OxanaDoroshkevich)
* **Christoph Doerr** - *Developer* - [Christoph Doerr](https://github.com/ChristophDoerr)
* **Marco Himmelstein** - *Developer* - [Marco Himmelstein](https://github.com/himmelst94)
* **Frederick Wurfer** - *Developer* - [Frederick Wurfer](https://github.com/FreddyWurfer)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

### License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* avaliable in play store
