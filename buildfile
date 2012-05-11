require 'buildr/java/cobertura'
require 'buildr-dependency-extensions'

HAMCREST = group('hamcrest-core', 'hamcrest-library', :under => 'org.hamcrest', :version => '1.3.RC2')

define 'time', :group => 'org.testinfected.time', :version => '1.1-SNAPSHOT' do
  extend PomGenerator
  
  compile.options.target = '1.6'
  test.with HAMCREST
  package :jar

  package_with_javadoc
  package_with_sources
end
