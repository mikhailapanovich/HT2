package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ManagePersonServlet extends HttpServlet {
	
	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;
	
	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;
	
	// Редактируемый Person в данной сессии 
	private Person sessionPerson;		//проблема в том что может быть только одна сессия на текущий момент

       
    public ManagePersonServlet() {
        // Вызов родительского конструктора.
    	super();
		
    	// Создание экземпляра телефонной книги.
        try {
			this.phonebook = Phonebook.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}               
    }

    // Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
    private String validatePersonFMLName(Person person) {
		String error_message = "";
		
		if (!person.validateFMLNamePart(person.getName(), false)) {
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		if (!person.validateFMLNamePart(person.getSurname(), false)) {
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		if (!person.validateFMLNamePart(person.getMiddlename(), true)) {
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		return error_message;
    }
    
    // Валидация телефонного немера и генерация сообщения об ошибке в случае невалидных данных.
    private String validatePhoneNumber(String number) {
		String error_message = "";
		
		if (!Phone.validatePhoneNumber(number)) {
			error_message += "Телефонный номер должен быть от 2 до 50 символов из цифр, знаков плюс, минус и знаков решетки.<br />";
		}
		
		return error_message;
    }
    
    // Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");
		
		// debug: uncomment to see post parameters
		System.out.println("GET");
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			System.out.println(entry.getKey() + " " + java.util.Arrays.toString(entry.getValue()));
		}
		System.out.println();
		
		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
        RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
        RequestDispatcher dispatcher_for_phones = request.getRequestDispatcher("/EditPhone.jsp");

		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		
		String phone_id = request.getParameter("phone_id");
		
		//TODO: если недостаточное количество параметров, то показать стартовую страницу + error_message
		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
        if (action == null) {
        	request.setAttribute("jsp_parameters", jsp_parameters);
            dispatcher_for_list.forward(request, response);
        } else {
        	switch (action) {
        		// Добавление записи.
        		case "add":
        			// Создание новой пустой записи о пользователе.
        			sessionPerson = new Person();
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add");
        			jsp_parameters.put("next_action", "add_go");
        			jsp_parameters.put("next_action_label", "Добавить");
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        			break;
			
        		// Редактирование записи.
        		case "edit":        			
        			// Извлечение из телефонной книги информации о редактируемой записи.
        			if (id != null && !id.equals("")) {
        				sessionPerson = new Person(this.phonebook.getPerson(id));
        			}
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Сохранить");

        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        			break;
			
        		// Удаление записи.
        		case "delete":
        			
        			if (phonebook.deletePerson(id)) {
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        			} else {
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        			}

        			// Установка параметров JSP.
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_list.forward(request, response);
        			break;
        			
        		// Удаление номера телефона.
        		case "delete_phone":
        			
        			if (sessionPerson.deletePhoneNumber(phone_id)) {
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        			} else {
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        			}
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Сохранить");
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        			break;
       			
       			// Добавление номера телефона.
        		case "add_phone":   
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add_phone");
        			jsp_parameters.put("next_action", "add_phone_go");
        			jsp_parameters.put("next_action_label", "Добавить номер");
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_phones.forward(request, response);
        			break;
        		
        		// Редактирование номера телефона.
        		case "edit_phone":  

        			String phoneNumber = sessionPerson.getPhones().get(phone_id);
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit_phone");
        			jsp_parameters.put("next_action", "edit_phone_go");
        			jsp_parameters.put("next_action_label", "Сохранить номер");
        			jsp_parameters.put("phone_id", phone_id);
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("phone_number", phoneNumber);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_phones.forward(request, response);
        			break;
       		}
        }
		
	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");
		
		// debug: uncomment to see post parameters
		System.out.println("POST");
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			System.out.println(entry.getKey() + " " + java.util.Arrays.toString(entry.getValue()));
		}
		System.out.println();

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_phones = request.getRequestDispatcher("/EditPhone.jsp");
		
		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");
		String phone_id = request.getParameter("phone_id");
		String phoneNumber;
		
		String error_message = "";
		
		switch (action)
    	{
    		// Добавление записи.
    		case "add_go":
    			// Создание записи на основе данных из формы.
    			sessionPerson = new Person(request.getParameter("name"), request.getParameter("surname"), request.getParameter("middlename"));

    			// Валидация ФИО.
    			error_message = this.validatePersonFMLName(sessionPerson); 
    			
    			// Если данные верные, можно производить добавление.
    			if (error_message.equals("")) {
    				
    				// Если запись удалось добавить...
    				if (this.phonebook.addPerson(sessionPerson)) {
    					
    					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
    					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
    				}
    				// Если запись НЕ удалось добавить...
    				else {
    					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
    					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
    				}

    				// Установка параметров JSP.
    				request.setAttribute("jsp_parameters", jsp_parameters);
    	        
    				// Передача запроса в JSP.
    				dispatcher_for_list.forward(request, response);
    			}
    			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
    			else {
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add");
        			jsp_parameters.put("next_action", "add_go");
        			jsp_parameters.put("next_action_label", "Добавить");
        			jsp_parameters.put("error_message", error_message);
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
    			}
    			break;
    		
    		// Редактирование записи.
    		case "edit_go":
    			// Обновление сессии на основе данных из формы.
    			sessionPerson.setName(request.getParameter("name"));
    			sessionPerson.setSurname(request.getParameter("surname"));
    			sessionPerson.setMiddlename(request.getParameter("middlename"));

    			// Валидация ФИО.
    			error_message = this.validatePersonFMLName(sessionPerson); 
    			
    			// Если данные верные, можно производить добавление.
    			if (error_message.equals("")) { 
    				
    				// Если запись удалось обновить...
    				if (this.phonebook.updatePerson(sessionPerson)) {

    					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
    					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
    				}
    				// Если запись НЕ удалось обновить...
    				else {
    					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
    					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
    				}

    				// Установка параметров JSP.
    				request.setAttribute("jsp_parameters", jsp_parameters);
    	        
    				// Передача запроса в JSP.
    				dispatcher_for_list.forward(request, response);
    			}
    			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
    			else {
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Сохранить");
        			jsp_parameters.put("error_message", error_message);

        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);    			
    			}
    			break;
    		
    		// Добавление номера телефона.
    		case "add_phone_go":
    			// Получение номера.
    			phoneNumber = request.getParameter("phone_number");

    			// Валидация телефонного номера.
    			error_message = this.validatePhoneNumber(phoneNumber); 
    			
    			// Если данные верные, можно производить добавление.
    			if (error_message.equals("") && sessionPerson.addPhoneNumber(phoneNumber)) {
        			// Подготовка параметров для JSP.
    				jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Сохранить");
        			jsp_parameters.put("error_message", error_message);
    				
    				// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
    				request.setAttribute("jsp_parameters", jsp_parameters);
    	        
    				// Передача запроса в JSP.
    				dispatcher_for_manager.forward(request, response);
    			} else {
    				if (error_message.equals("")) {
    					error_message = "Запись с таким номером уже существует";
    				}
    				
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add_phone");
        			jsp_parameters.put("next_action", "add_phone_go");
        			jsp_parameters.put("next_action_label", "Добавить номер");
        			jsp_parameters.put("error_message", error_message);
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			request.setAttribute("phone_number", phoneNumber);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_phones.forward(request, response);
    			}
    			break;
			
    		case "edit_phone_go":
    			// Получение номера.
    			phoneNumber = request.getParameter("phone_number");

    			// Валидация телефонного номера.
    			error_message = this.validatePhoneNumber(phoneNumber); 
    			
    			// Если данные верные, можно изменить номер.
    			if (error_message.equals("") && sessionPerson.setPhoneNumber(phone_id, phoneNumber)) {
        			// Подготовка параметров для JSP.
    				jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("next_action_label", "Сохранить");
        			jsp_parameters.put("error_message", error_message);
    				
    				// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
    				request.setAttribute("jsp_parameters", jsp_parameters);
    	        
    				// Передача запроса в JSP.
    				dispatcher_for_manager.forward(request, response);
    			} else {
    				if (error_message.equals("")) {
    					error_message = "Запись с таким номером уже существует";
    				}
    				
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit_phone");
        			jsp_parameters.put("next_action", "edit_phone_go");
        			jsp_parameters.put("next_action_label", "Сохранить номер");
        			jsp_parameters.put("phone_id", phone_id);
        			jsp_parameters.put("error_message", error_message);
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", sessionPerson);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			request.setAttribute("phone_number", phoneNumber);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_phones.forward(request, response);
    			}
    			break;
    	}
	}
}
