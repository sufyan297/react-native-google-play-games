module.exports = {
  dependency: {
    platforms: {
      android: {
        sourceDir: './android',
        packageImportPath:
          'import com.reactnativegoogleplaygames.GooglePlayGamesPackage;',
        packageInstance: 'new GooglePlayGamesPackage()',
      },
      ios: null,
    },
  },
};
