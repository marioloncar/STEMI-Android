# STEMI

<b>Android Version of STEMI App Controller </b> <br><br>
<b>Android developer:</b> Loncar Mario <br>
<b>iOS developer:</b> Abou Aldan Jasmin <br>
<b>UI/UX designer:</b> Jurcic Nina <br>

<b>URL:</b> http://www.stemi.education <br>
<b>Google Play Store:</b> https://play.google.com/store/apps/details?id=com.stemi.STEMIHexapod <br>
<b>Demo:</b> https://www.youtube.com/watch?v=31v0bN561qU <br>

<b>About STEMI:</b> STEMI Hexapod is a Marvel of Science, Technology, Engineering and Mathematics. Possibly the coolest item you might set your eyes and put hands on. From the way it looks, to the way it moves and sounds – it instantly captures everyone’s attention. A true pinnacle of Innovation.
And you can create one all by yourself. We’ll show you how. <br>
<b>About STEMI App:</b> We almost never control another physical thing with our beloved device. The feeling of commanding your robot with a smartphone can hardly be described. You have to simply try it. The app takes advantage of the modern technology such as gyroscope in your smartphone. This allows for the calculation of orientation and rotation of the STEMI’s body. The result is – a dancing robot moving in the rhythm of your hand! <br>

## STEMI Library

https://github.com/marioloncar/STEMI_Hexapod

This app uses STEMI library for communicating with robot and can be used as example app for Library usage. 

<br>
<br>
# Contributing to STEMI Hexapod Commander

## Git and our Branching model

### Git

We use [Git](http://git-scm.com/) as our [version control
system](http://en.wikipedia.org/wiki/Revision_control), so the best way to
contribute is to learn how to use it and put your changes on a Git repository.
There's a plenty of documentation about Git -- you can start with the [Pro Git
book](http://git-scm.com/book/).

### Forks + GitHub Pull requests

We use the famous
[gitflow](http://nvie.com/posts/a-successful-git-branching-model/) to manage our
branches.

Summary of our git branching model:
- Fork the desired repository on GitHub to your account;
- Clone your forked repository locally
  (`git clone git@github.com:your-username:repository-name.git`);
- Create a new branch off of `develop` with a descriptive name (for example:
  `feature/portuguese-sentiment-analysis`, `hotfix/bug-on-downloader`). You can
  do it switching to `develop` branch (`git checkout develop`) and then
  creating a new branch (`git checkout -b name-of-the-new-branch`);
- Do many small commits on that branch locally (`git add files-changed`,
  `git commit -m "Add some change"`);
- Push to your fork on GitHub (with the name as your local branch:
  `git push origin branch-name`);
- Create a pull request using the GitHub Web interface (asking us to pull the
  changes from your new branch and add to our `develop` branch);
- Wait for comments.


### Tips

- Write [helpful commit
  messages](http://robots.thoughtbot.com/5-useful-tips-for-a-better-commit-message).
- Anything in the `develop` branch should be deployable.
- Never use `git add .`: it can add unwanted files;
- Avoid using `git commit -a` unless you know what you're doing;
- Check every change with `git diff` before adding them to the index (stage
  area) and with `git diff --cached` before commiting;
- If you have push access to the main repository, please do not commit directly
  to `develop`: your access should be used only to accept pull requests; if you
  want to make a new feature, you should use the same process as other
  developers so you code will be reviewed.