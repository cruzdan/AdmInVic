package mx.uam.ayd.proyecto.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/** 
 * Esta clase permite crear la BD y simplifica la obtencion de conexiones de la base de datos 
 *
 */
public class ManejadorBaseDatos {
	// El logger
	static Logger log = Logger.getRootLogger();
	
	// Referencia a la instancia unica del manejador de la BD
	private static ManejadorBaseDatos m_manejador = null;

	// Informacion del driver
	private static String framework = "embedded";
	private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private static String protocol = "jdbc:derby:";
	
	// Nombre de la base de datos
	private String m_nombreBD = "BasePrueba2";
	
	// La conexion
	private Connection m_conexion = null;

	static {
		PropertyConfigurator.configure("res/log4j_config.properties");
	}
	
	/**
	 * Este manejador implementa el patron Singleton, por ello el constructor es privado
	 */
	private ManejadorBaseDatos() throws DatabaseException
	{

		try
		{
	        /*
	           La instalacion del driver ocurre cuando se carga la clase
	           en un entorno embebido esto inicia a Derby ya que no esta ejecutandose
	         */			
	        Class.forName(driver).newInstance();
	
	        // Se puede especificar un usuario y un password
	        Properties props = new Properties();
	        //props.put("user", "user1");
	        //props.put("password", "user1");
	
	        /*
	           En la conexion se especifica create=true para crear la base de datos
	           la base de datos se crea bajo el directorio actual. Para borrarla,
	           basta borrar el directorio que se creo.
	         */
	        m_conexion = DriverManager.getConnection(protocol + m_nombreBD+ ";create=true", props);
	
	        // AutoCommit = true para que los cambios se repercutan inmediatamente.
	        m_conexion.setAutoCommit(true);
		}
		catch(Exception ex)
		{
			log.error(ex);
			throw new DatabaseException("Problema al levantar base de datos",ex);
		}
	}
	
	/**
	 * Este metodo permite obtener un Connection a partir del cual se pueden
	 * crear Statements para ejectuar codigo SQL.
	 * 
	 * La primera vez que se invoca este metodo, se checa si ya se creo la instancia
	 * del Manejador de la Base de Datos. Si aun no ha sido creado, se instancia. Esto
	 * solo puede ocurrir una vez, por lo tanto esto representa el patron singleton.
	 * 
	 * @return
	 */
	public static Connection getConnection() throws DatabaseException
	{
		if (m_manejador == null)
		{
			m_manejador = new ManejadorBaseDatos();
			log.info("El manejador de la base de datos ha sido creado");
		}
		
		return m_manejador.m_conexion;
	}
	
	/**
	 * El metodo finalize se sobre-escribe para cerrar la conexión y apagar la base de datos cuando
	 * el colector de basura recoge al manejador
	 * 
	 */
	public static void shutdown()	
	{
		log.info("Apagando base de datos...");

		// Si nunca se creo el manejador no hay necesidad de apagarlo...
		if (m_manejador == null)
		{
			return;
		}

		
		try
		{
			// Se cierra la conexion
			m_manejador.m_conexion.close();
		}
		catch(SQLException ex)
		{
			log.error(ex);
		}

        /*
           In embedded mode, an application should shut down Derby.
           If the application fails to shut down Derby explicitly,
           the Derby does not perform a checkpoint when the JVM shuts down, which means
           that the next connection will be slower.
           Explicitly shutting down Derby with the URL is preferred.
           This style of shutdown will always throw an "exception".
         */
        boolean gotSQLExc = false;

        if (framework.equals("embedded"))
        {
            try
            {
                DriverManager.getConnection(protocol+";shutdown=true");
            }
            catch (SQLException se)
            {
                gotSQLExc = true;
            }

            if (!gotSQLExc)
            {
                log.warn("La base de datos no se apago de forma normal");
            }
            else
            {
                log.info("La base de datos se apago normalmente");
            }
        }
    }
	
}
