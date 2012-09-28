package org.hibernate.tutorial;

import org.hibernate.Session;
import org.hibernate.tutorial.web.*;

import java.util.*;

import org.hibernate.tutorial.domain.Event;
import org.hibernate.tutorial.domain.Person;

import org.hibernate.tutorial.util.HibernateUtil;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

public class EventManager {

    public static void main(String[] args) {
        EventManager mgr = new EventManager();

        if (args[0].equals("store")) {
            mgr.createAndStoreEvent("My Event", new Date());
        }
        else if (args[0].equals("list")) {
            List events = mgr.listEvents();
            for (int i = 0; i < events.size(); i++) {
                Event theEvent = (Event) events.get(i);
                System.out.println(
                        "Event: " + theEvent.getTitle() + " Time: " + theEvent.getDate()
                );
            }
        }
		else if (args[0].equals("addpersontoevent")) {
            Long eventId = mgr.createAndStoreEvent("My Event", new Date());
            Long personId = mgr.createAndStorePerson("Foo", "Bar", 25);
            mgr.addPersonToEvent(personId, eventId);
            System.out.println("Added person " + personId + " to event " + eventId);
        }
		else if(args[0].equals("addemailtoperson")) {
			String email = "rodders.miller@gmail.com";
			Long personId = mgr.createAndStorePerson("Foo-ooo", "Bar", 25);
			mgr.addEmailToPerson(personId, email);
			System.out.println("Added e-mail " + email + " to person " + personId); 
		}
		else if(args[0].equals("addemailtopersoneager")) {
			String email = "rodders.miller@gmail.com";
			Long personId = mgr.createAndStorePerson("Foo-ooo-eager", "Bar", 25);
			mgr.addEmailToPersonEager(personId, email);
			System.out.println("Added e-mail " + email + " to person " + personId); 
		}
		else if(args[0].equals("startServer")) {
			

			Server server = new Server(8080);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			server.setHandler(context);
	 
			context.addServlet(new ServletHolder(new EventManagerServlet()),"/*");

			try {
				server.start();
				server.join();	
			} catch(Exception exp)
			{
			
			}
				
			/*
			Handler handler=new AbstractHandler()
			{
				public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException
				{
					response.setContentType("text/html");
					response.setStatus(HttpServletResponse.SC_OK);
					response.getWriter().println("<h1>Hello</h1>");
					((Request)request).setHandled(true);
				}
			};
			 
			Server server = new Server(8080);
			server.setHandler(handler);
			
			try {
				server.start();
			} catch(Exception exp)
			{
			
			}
			
			*/
		}

        HibernateUtil.getSessionFactory().close();
    }

    private Long createAndStoreEvent(String title, Date theDate) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Event theEvent = new Event();
        theEvent.setTitle(title);
        theEvent.setDate(theDate);
        session.save(theEvent);

        session.getTransaction().commit();
		
		return theEvent.getId();
	}
	
	private Long createAndStorePerson(String firstname, String lastname, int age) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person thePerson = new Person();
		
		thePerson.setFirstname(firstname);
		thePerson.setLastname(lastname);
		thePerson.setAge(age);
        session.save(thePerson);

        session.getTransaction().commit();
		
		return thePerson.getId();
    }
	
    private List listEvents() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List result = session.createQuery("from Event").list();
        session.getTransaction().commit();
        return result;
    }
	
	private void addPersonToEvent(Long personId, Long eventId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session.load(Person.class, personId);
        Event anEvent = (Event) session.load(Event.class, eventId);
        aPerson.addToEvent(anEvent);

        session.getTransaction().commit();
    }
	
	private void addEmailToPerson(Long personId, String emailAddress) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session.load(Person.class, personId);
        // adding to the emailAddress collection might trigger a lazy load of the collection
        aPerson.getEmailAddresses().add(emailAddress);

        session.getTransaction().commit();
    }
	
	private void addEmailToPersonEager(Long personId, String emailAddress){
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session
                .createQuery("select p from Person p left join fetch p.emailAddresses where p.id = :pid")
                .setParameter("pid", personId)
                .uniqueResult(); // Eager fetch the collection so we can use it detached
				
        session.getTransaction().commit();

        // End of first unit of work

        aPerson.getEmailAddresses().add(emailAddress); // aPerson (and its collection) is detached

        // Begin second unit of work

        Session session2 = HibernateUtil.getSessionFactory().getCurrentSession();
        session2.beginTransaction();
        session2.update(aPerson); // Reattachment of aPerson

        session2.getTransaction().commit();
    }
	
	
	
}