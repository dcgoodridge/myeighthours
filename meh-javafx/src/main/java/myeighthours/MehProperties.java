package myeighthours;


import org.aeonbits.owner.Config;

@Config.Sources( { "file:meh.properties" } )
public interface MehProperties extends Config
{

  @Key( "user" )
  @DefaultValue( "TELTRONICDOM\\user" )
  String user();

  @Key( "log" )
  @DefaultValue( "info" )
  String getLogLevel();

  /**
   * Maximum time between webcrawler tasks in secs.
   * @return Seconds
   */
  @Key( "webscrap_interval" )
  @DefaultValue( "3600" )
  int webscrapInterval();

  /**
   * Maximum time between main service update checks in secs.
   * @return Seconds
   */
  @Key( "mehservice_interval" )
  @DefaultValue( "10" )
  int mehserviceInterval();

  @Key( "webscrap_driver" )
  @DefaultValue( "HTMLUNIT" )
  String webscrapDriver();

  @Key( "minimizetotray" )
  @DefaultValue( "false" )
  boolean minimizetotrayIsEnabled();

  @Key( "debug" )
  @DefaultValue( "false" )
  boolean debugIsEnabled();

}