# Popular Movies

This project retrieves a list of newly released movies & presents them to the user in the form of movie posters. When the user clicks on a poster they will be presented with detailed movie information. That information includes a synopsis, a release date, reviews, trailers & a rating. This information comes from [TheMovieDB](www.themoviedb.org).

## Installation

Open up a terminal and type the following:

```
$ cd /folder/where/you/want/my/project/cloned/to
$ git clone https://github.com/DanielKaparunakis/Popular-Movies.git
```

If done correctly, you should see something similar to:

```
Cloning into 'Popular-Movies'...
remote: Counting objects: 354, done.
remote: Compressing objects: 100% (10/10), done.
remote: Total 354 (delta 15), reused 14 (delta 14), pack-reused 330
Receiving objects: 100% (354/354), 176.86 KiB | 0 bytes/s, done.
Resolving deltas: 100% (162/162), done.
Checking connectivity... done.
```

## Design Decisions

For image caching, a quick [Android Arsenal](https://android-arsenal.com/) search led me to [Picasso](https://github.com/square/picasso), maintained by Square, & [Glide](https://github.com/bumptech/glide), maintained by Bumptech. I wanted something simple & light so I picked Picasso. There are a couple of more libraries worth a look as well, for that I recommend this [StackOverflow](http://stackoverflow.com/questions/29363321/picasso-v-s-imageloader-v-s-fresco-vs-glide) link that compares all of them.

Other than Picasso, I used all standard Google APIs. I used ASyncTask for network calls & I used a content provider for all database operations.

## Future Updates

* Refactor ASyncTasks into RxJava observables.
* Refactor boiler plate network code into a Retrofit service.
* Implement an MVP architecture using Dagger 2.

## License

Copyright 2016 by Daniel Kaparunakis

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.





