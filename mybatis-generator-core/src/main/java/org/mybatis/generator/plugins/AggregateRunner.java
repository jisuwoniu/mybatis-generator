package org.mybatis.generator.plugins;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 将DelUpdateByExamPlugin集成进来的Runner，代替了原有的ShellRunner功能，具体：
 * 
 * <pre>
 *    解析输入的配置文件，插入DelUpdateByExamPlugin，交给generator继续解析
 * </pre>
 * 
 * @author mingxu
 *
 */
public class AggregateRunner {
    private static final String CONFIG_FILE = "-configfile"; //$NON-NLS-1$
    private static final String OVERWRITE = "-overwrite"; //$NON-NLS-1$
    private static final String CONTEXT_IDS = "-contextids"; //$NON-NLS-1$
    private static final String TABLES = "-tables"; //$NON-NLS-1$
    private static final String VERBOSE = "-verbose"; //$NON-NLS-1$
    private static final String FORCE_JAVA_LOGGING = "-forceJavaLogging"; //$NON-NLS-1$
    private static final String HELP_1 = "-?"; //$NON-NLS-1$
    private static final String HELP_2 = "-h"; //$NON-NLS-1$

    public static void main(String[] args) {
//        if (args.length == 0) {
//            usage();
//            System.exit(0);
//            return; // only to satisfy compiler, never returns
//        }

        Map<String, String> arguments = parseCommandLine(args);

        if (arguments.containsKey(HELP_1)) {
            usage();
            System.exit(0);
            return; // only to satisfy compiler, never returns
        }

//        if (!arguments.containsKey(CONFIG_FILE)) {
//            writeLine(getString("RuntimeError.0")); //$NON-NLS-1$
//            return;
//        }

        List<String> warnings = new ArrayList<String>();

        String configfile = arguments.get(CONFIG_FILE);
        if(null == configfile) configfile = ".";
        
        File configurationFile = new File(configfile);
        if (!configurationFile.exists()) {
            writeLine(getString("RuntimeError.1", configfile)); //$NON-NLS-1$
            return;
        }

        Set<String> fullyqualifiedTables = new HashSet<String>();
        if (arguments.containsKey(TABLES)) {
            StringTokenizer st = new StringTokenizer(arguments.get(TABLES), ","); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    fullyqualifiedTables.add(s);
                }
            }
        }

        Set<String> contexts = new HashSet<String>();
        if (arguments.containsKey(CONTEXT_IDS)) {
            StringTokenizer st = new StringTokenizer(
                    arguments.get(CONTEXT_IDS), ","); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    contexts.add(s);
                }
            }
        }

        try {
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(aggrPlugins(new File(configfile)));

            DefaultShellCallback shellCallback = new DefaultShellCallback(
                    arguments.containsKey(OVERWRITE));

            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);

            ProgressCallback progressCallback = arguments.containsKey(VERBOSE) ? new VerboseProgressCallback()
                    : null;

            myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);

        } catch (XMLParserException e) {
            writeLine(getString("Progress.3")); //$NON-NLS-1$
            writeLine();
            for (String error : e.getErrors()) {
                writeLine(error);
            }

            return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InvalidConfigurationException e) {
            writeLine(getString("Progress.16")); //$NON-NLS-1$
            for (String error : e.getErrors()) {
                writeLine(error);
            }
            return;
        } catch (InterruptedException e) {
            // ignore (will never happen with the DefaultShellCallback)
            ;
        } catch(Exception e) {
        	e.printStackTrace();
        	return;
        }

        for (String warning : warnings) {
            writeLine(warning);
        }

        if (warnings.size() == 0) {
            writeLine(getString("Progress.4")); //$NON-NLS-1$
        } else {
            writeLine();
            writeLine(getString("Progress.5")); //$NON-NLS-1$
        }
    }

    private static void usage() {
        String lines = getString("Usage.Lines"); //$NON-NLS-1$
        int iLines = Integer.parseInt(lines);
        for (int i = 0; i < iLines; i++) {
            String key = "Usage." + i; //$NON-NLS-1$
            writeLine(getString(key));
        }
    }
    
    /**
     * 将必选的Plugin聚合到用户的配置文件中
     * 
     * @param configFile
     * @return
     * @throws Exception
     */
    private static Reader aggrPlugins(File configFile) throws Exception {
    	//文件为目录的情况下默认读取mybatis-generator.xml这个文件
    	if(configFile.isDirectory()) {
    		File n = new File(configFile.getPath() + "/mybatis-generator.xml");
    		if(!n.exists()) {
    			throw new Exception("mybatis-generator.xml不存在，请检查路径或者指定完整的配置文件路径");
    		}
    		
    		configFile = n;
    	}
    	
    	Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
    	XPathFactory xFac = XPathFactory.newInstance();
    	XPathExpression expr = xFac.newXPath()
    		    .compile("//generatorConfiguration/context");
    	
    	//将DelUpdateByExamPlugin作为必选的Plugin配置，同时允许用户做其他的配置
    	Element plugin = doc.createElement("plugin");
    	plugin.setAttribute("type", "org.mybatis.generator.plugins.DelUpdateByExamPlugin");
    	
    	//将plugin标签插入到合适的位置(property之后，其他元素之前)
    	Element context = (Element) 
    			((NodeList)expr.evaluate(doc, XPathConstants.NODESET)).item(0);
    	NodeList prop = context.getChildNodes();
    	for(int i = 0; i < prop.getLength(); i++) {
    		Node n = prop.item(i);
    		if(!"property".equals(n.getNodeName())
    				&& (n instanceof Element)) {
    			context.insertBefore(plugin, prop.item(i));
    			break;
    		} 
    		    	
    	}
    	
    	//序列化插入后的结果
    	DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

    	DOMImplementationLS impl = 
    	    (DOMImplementationLS)registry.getDOMImplementation("LS");

    	LSSerializer writer = impl.createLSSerializer();
    	String str = writer.writeToString(doc);
    	return new StringReader(str);
    }

    private static void writeLine(String message) {
        System.out.println(message);
    }

    private static void writeLine() {
        System.out.println();
    }

    private static Map<String, String> parseCommandLine(String[] args) {
        List<String> errors = new ArrayList<String>();
        Map<String, String> arguments = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            if (CONFIG_FILE.equalsIgnoreCase(args[i])) {
                if ((i + 1) < args.length) {
                    arguments.put(CONFIG_FILE, args[i + 1]);
                } else {
                    errors.add(getString(
                            "RuntimeError.19", CONFIG_FILE)); //$NON-NLS-1$
                }
                i++;
            } else if (OVERWRITE.equalsIgnoreCase(args[i])) {
                arguments.put(OVERWRITE, "Y"); //$NON-NLS-1$
            } else if (VERBOSE.equalsIgnoreCase(args[i])) {
                arguments.put(VERBOSE, "Y"); //$NON-NLS-1$
            } else if (HELP_1.equalsIgnoreCase(args[i])) {
                arguments.put(HELP_1, "Y"); //$NON-NLS-1$
            } else if (HELP_2.equalsIgnoreCase(args[i])) {
                // put HELP_1 in the map here too - so we only
                // have to check for one entry in the mainline
                arguments.put(HELP_1, "Y"); //$NON-NLS-1$
            } else if (FORCE_JAVA_LOGGING.equalsIgnoreCase(args[i])) {
                LogFactory.forceJavaLogging();
            } else if (CONTEXT_IDS.equalsIgnoreCase(args[i])) {
                if ((i + 1) < args.length) {
                    arguments.put(CONTEXT_IDS, args[i + 1]);
                } else {
                    errors.add(getString(
                            "RuntimeError.19", CONTEXT_IDS)); //$NON-NLS-1$
                }
                i++;
            } else if (TABLES.equalsIgnoreCase(args[i])) {
                if ((i + 1) < args.length) {
                    arguments.put(TABLES, args[i + 1]);
                } else {
                    errors.add(getString("RuntimeError.19", TABLES)); //$NON-NLS-1$
                }
                i++;
            } else {
                errors.add(getString("RuntimeError.20", args[i])); //$NON-NLS-1$
            }
        }

        if (!errors.isEmpty()) {
            for (String error : errors) {
                writeLine(error);
            }

            System.exit(-1);
        }

        return arguments;
    }
}
