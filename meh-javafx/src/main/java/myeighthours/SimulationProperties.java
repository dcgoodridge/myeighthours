package myeighthours;


import org.aeonbits.owner.Config;

@Config.Sources( { "file:simulation.properties" } )
public interface SimulationProperties extends Config
{

  @Key( "active" )
  @DefaultValue( "false" )
  boolean isActive();

  /**
   * Tiempo que tarda en hacer un Webcrawl simulado
   * @return Segundos
   */
  @Key( "webscrap_time" )
  @DefaultValue( "5" )
  int getWebscrapTime();

}