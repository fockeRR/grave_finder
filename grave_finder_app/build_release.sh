cp build_config_files/values_release.xml res/values/build_values.xml
#run build
ant -Dkey.store.password=$GF_STORE_PASS -Dkey.alias.password=$GF_ALIAS_PASS -Dkey.store=$GF_KEY_STORE_PATH -Dkey.alias=$KEY_STORE_ALIAS custom-production-build clean release copy-binaries
cp build_config_files/values_default.xml res/values/build_values.xml
