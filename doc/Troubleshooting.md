<h1>Troubleshooting</h1>

### invalid model format

Using OSX with a non-standard layout (for example the french layout) you get the following error: 
```
error: invalid model format
```

This is because Wapiti is not reading the binary files correctly. 

Workaround:
```
export LC_ALL=C
```

See [here](https://github.com/kermitt2/grobid/issues/142#issuecomment-253497513) the open issue. 